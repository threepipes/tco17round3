import os
import sys
import shutil
import subprocess
import time

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
    # subprocess.run(command, cwd='../', shell=True)
    p = subprocess.Popen(command, cwd='../', shell=True, stdout=subprocess.PIPE)
    stdout, stderr = p.communicate()
    shutil.move(src_path, dst_path)
    shutil.move(tmp_path, src_path)
    return 'BUILD SUCCESSFUL' in stdout.decode('utf-8')


def copy_jar():
    jar_dir = '../build/libs/'
    file_list = os.listdir(jar_dir)
    if not file_list:
        return False
    newest = sorted(
        file_list,
        key=lambda f: -os.stat(jar_dir + f).st_mtime
    )[0]
    # print('created: %f, time: %f' % (time.time(), os.stat(jar_dir + newest).st_mtime))
    # diff = time.time() - os.stat(jar_dir + newest).st_mtime
    # print('diff: %f' % diff)
    print('copy from %s to %s' % (
        jar_dir + newest,
        os.getenv('DATA_PATH') + 'tco17_3/submit/' + newest
    ))
    shutil.copy(
        jar_dir + newest,
        os.getenv('DATA_PATH') + 'tco17_3/submit/' + newest
    )


if __name__ == '__main__':
    local = False
    if len(sys.argv) > 1:
        if sys.argv[1] == 'local':
            local = True

    if local:
        if build_test_jar():
            copy_jar()
    else:
        create(local)
