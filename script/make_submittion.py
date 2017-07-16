import os
import sys
import shutil
import subprocess
import time

from datetime import datetime

project_name = 'tco17_3'

ext = 'java'

src_path = '../src/main/java/PoisonedWine.java'
dst_path = '../data/PoisonedWine.java'
history_path = os.getenv('DATA_PATH') + project_name + '/source/'


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


def create(local=False, path=dst_path, message=None):
    source = get_formatted(local)
    with open(dst_path, 'w', encoding='utf-8') as f:
        if message:
            f.write('//' + message + '\n')
        f.write(source)
    timestamp = datetime.now().strftime("%m%d%H%M%S")
    filename = timestamp
    if message:
        filename += '_' + message
    filename += '.' + ext
    shutil.copy(dst_path, history_path + timestamp + '.' + ext)


def build_test_jar(message):
    command = 'gradle jar'
    create(local=True, message=message)
    tmp_path = '../data/tmp'
    shutil.move(src_path, tmp_path)
    shutil.move(dst_path, src_path)
    # subprocess.run(command, cwd='../', shell=True)
    p = subprocess.Popen(command, cwd='../', shell=True, stdout=subprocess.PIPE)
    stdout, stderr = p.communicate()
    shutil.move(src_path, dst_path)
    shutil.move(tmp_path, src_path)
    return 'BUILD SUCCESSFUL' in stdout.decode('utf-8')


def copy_jar(name=None):
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
        os.getenv('DATA_PATH') + project_name + '/submit/' + newest
    ))
    filename = newest
    if name:
        filename = name + '.jar'
    shutil.copy(
        jar_dir + newest,
        os.getenv('DATA_PATH') + project_name + '/submit/' + filename
    )


if __name__ == '__main__':
    local = False
    sub = True
    message = None
    name = None
    for arg in sys.argv[1:]:
        if arg == 'local':
            local = True
        if arg == '--nosub':
            sub = False
        if arg.startswith('-m='):
            message = arg[3:]
        if arg.startswith('--name='):
            name = arg[7:]

    if local:
        if build_test_jar(name) and sub:
            copy_jar(name)
    else:
        create(local, message=message)
