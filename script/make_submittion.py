src_path = '../src/main/java/PoisonedWine.java'
dst_path = '../data/PoisonedWine.java'

source = ''
mode = True
with open(src_path, encoding='utf-8') as f:
    for line in f:
        if '--- cut start ---' in line:
            mode = False
        elif '--- cut end ---' in line:
            mode = True
            continue
        if mode:
            source += line

with open(dst_path, 'w', encoding='utf-8') as f:
    f.write(source)
