import sys
import shutil
import subprocess

src_path = '../src/main/java/PoisonedWine.java'
dst_path = '../data/PoisonedWine.java'


def list_in_line(strs, line):
    for st in strs:
        if st in line:
            return True
    return False


def get_formatted(local=False):
    source = ''
    mode = True
    starts = ['--- cut start ---']
    ends = ['--- cut end ---']
    if not local:
        starts.append('--- sub start ---')
        ends.append('--- sub end ---')

    with open(src_path, encoding='utf-8') as f:
        for line in f:
            if list_in_line(starts, line):
                mode = False
            if list_in_line(ends, line):
                mode = True
                continue
            if mode:
                source += line
    return source


def create(local=False, path=dst_path):
    source = get_formatted(local)
    with open(dst_path, 'w', encoding='utf-8') as f:
        f.write(source)


def build_test_jar():
    command = 'gradle jar'
    create(local=True)
    tmp_path = '../data/tmp'
    shutil.move(src_path, tmp_path)
    shutil.move(dst_path, src_path)
    subprocess.run(command, cwd='../', shell=True)
    shutil.move(src_path, dst_path)
    shutil.move(tmp_path, src_path)


if __name__ == '__main__':
    local = False
    if len(sys.argv) > 1:
        if sys.argv[1] == 'local':
            local = True

    if local:
        build_test_jar()
    else:
        create(local)
