"""Optional authoring tool for public UI strings; never used by builds or gameplay.

Writes machine translation drafts under build/localization/drafts for review.
Run with Python 3.10+: python tools/localization/import-drafts.py de fr ja
"""

import argparse
import concurrent.futures
import hashlib
import json
from pathlib import Path
import re
import time
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'src/main/resources/assets/thirst_was_upgraded/lang/en_us.json'
DRAFTS = ROOT / 'build/localization/drafts'


def translate(target, source):
    draft = DRAFTS / f'{target}.json'
    fingerprint = hashlib.sha256(json.dumps(source, sort_keys=True).encode('utf-8')).hexdigest()
    fingerprint_file = DRAFTS / f'{target}.sha256'
    if draft.exists() and fingerprint_file.exists() and fingerprint_file.read_text() == fingerprint:
        return f'CACHED {target}'

    # Numbered markers preserve the association between values and translation keys.
    values = list(dict.fromkeys(source.values()))
    # Explicitly mention water so "drinking" is not interpreted as alcohol use.
    context = {'Drinking Upgrade': 'Automatic Water Drinking Upgrade'}
    query = '\n'.join(f'[[{i:03d}]]\n{context.get(value, value)}' for i, value in enumerate(values))
    url = 'https://translate.googleapis.com/translate_a/single?' + urllib.parse.urlencode({
        'client': 'gtx', 'sl': 'en', 'tl': target, 'dt': 't', 'q': query,
    })
    for attempt in range(3):
        try:
            request = urllib.request.Request(url, headers={'User-Agent': 'ThirstWasUpgraded-localization/1.0'})
            with urllib.request.urlopen(request, timeout=25) as response:
                data = json.load(response)
            text = ''.join(part[0] for part in data[0] if part[0])
            matches = re.findall(r'\[\[?\s*(\d{3})\s*\]\]?\s*(.*?)(?=\[\[?\s*\d{3}\s*\]\]?|$)', text, re.S)
            translated = {int(index): re.sub(r'\s+', ' ', value).strip() for index, value in matches}
            if set(translated) != set(range(len(values))) or not all(translated.values()):
                raise ValueError('Translation lost entry markers or values')
            by_value = {value: translated[i] for i, value in enumerate(values)}
            result = {key: by_value[value] for key, value in source.items()}
            draft.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
            fingerprint_file.write_text(fingerprint, encoding='utf-8')
            return f'DRAFT {target}'
        except Exception as error:
            if attempt == 2:
                return f'UNAVAILABLE {target}: {error}'
            time.sleep(1 + attempt)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('targets', nargs='+', help='Translation service language codes, for example de fr ja')
    args = parser.parse_args()
    if any(not re.fullmatch(r'[a-zA-Z-]+', target) for target in args.targets):
        parser.error('Language codes must contain only letters and hyphens')
    source = json.loads(SOURCE.read_text(encoding='utf-8'))
    DRAFTS.mkdir(parents=True, exist_ok=True)
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        for result in pool.map(lambda target: translate(target, source), dict.fromkeys(args.targets)):
            print(result, flush=True)


if __name__ == '__main__':
    main()
