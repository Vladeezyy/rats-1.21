"""Where the server thread spent its JFR samples, as far as Rats code is concerned.

python3 tools/jfr_hot.py <recording.jfr> [top] [last seconds]
Prints the share of "Server thread" samples with a Rats frame on the stack, the Rats methods by inclusive samples (on
the stack) and by "own" samples (the innermost Rats frame, i.e. Rats code or the vanilla code it called), and the
methods of any code by inclusive samples. With [last seconds], only the samples of the end of the recording (the
stress test's rat phase is its last 70 s).
"""
import collections
import json
import os
import subprocess
import sys

MOD = ('com.github.alexthe666.rats', 'dev.vladeezyy')


def main():
    path = sys.argv[1]
    top = int(sys.argv[2]) if len(sys.argv) > 2 else 15
    last = float(sys.argv[3]) if len(sys.argv) > 3 else 0
    jfr = os.path.join(os.environ.get('JAVA_HOME', ''), 'bin', 'jfr') if os.environ.get('JAVA_HOME') else 'jfr'
    out = subprocess.run([jfr, 'print', '--json', '--stack-depth', '256', '--events', 'jdk.ExecutionSample', path], capture_output=True, text=True).stdout
    events = json.loads(out)['recording']['events']
    if last:
        times = [event['values']['startTime'] for event in events]
        end = max(times)
        from datetime import datetime, timedelta
        import re

        def parse(text):
            # "2026-10-20T07:10:13.607+03:00" or "...Z", any number of fraction digits (Python 3.9 wants 0, 3 or 6)
            m = re.match(r'(.*?T[\d:]+)(?:\.(\d+))?(Z|[+-]\d\d:\d\d)?$', text)
            base, frac, zone = m.group(1), (m.group(2) or '0'), m.group(3) or '+00:00'
            return datetime.fromisoformat(base + '.' + (frac + '000000')[:6] + ('+00:00' if zone == 'Z' else zone))
        limit = parse(end) - timedelta(seconds=last)
        events = [event for event in events if parse(event['values']['startTime']) >= limit]
    total = 0
    everything = collections.Counter()
    with_mod = 0
    inclusive = collections.Counter()
    innermost = collections.Counter()
    for event in events:
        values = event['values']
        thread = values.get('sampledThread') or {}
        if thread.get('javaName') != 'Server thread':
            continue
        total += 1
        frames = (values.get('stackTrace') or {}).get('frames') or []
        names = []
        for frame in frames:
            method = frame['method']
            names.append(method['type']['name'].replace('/', '.') + '.' + method['name'])
        for name in set(names):
            everything[name] += 1
        mod = [name for name in names if name.startswith(MOD) and '.selftest.' not in name]
        if mod:
            with_mod += 1
            for name in set(mod):
                inclusive[name] += 1
            innermost[mod[0]] += 1
    if not total:
        print('no server thread samples in', path)
        return
    print('server thread samples: %d, with Rats code on the stack: %d (%.1f%%)' % (total, with_mod, 100.0 * with_mod / total))
    print('Rats methods on the stack (inclusive):')
    for name, count in inclusive.most_common(top):
        print('  %5.1f%%  %s' % (100.0 * count / total, name))
    print('innermost Rats frame (its own code and the vanilla calls it makes):')
    for name, count in innermost.most_common(top):
        print('  %5.1f%%  %s' % (100.0 * count / total, name))
    print('any code on the stack (inclusive):')
    for name, count in everything.most_common(top * 3):
        print('  %5.1f%%  %s' % (100.0 * count / total, name))


if __name__ == '__main__':
    main()
