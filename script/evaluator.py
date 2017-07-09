import time
import os
import shutil
from datetime import datetime

from joblib import Parallel, delayed


working_dir = os.getenv('DATA_PATH')
submit_dir = working_dir + 'submit/'
eval_file = working_dir + 'tmp/tmp.jar' # jar前提
evaled_dir = working_dir + 'eval/'


def move_file(filename):
    timestamp = datetime.now().strftime("%m%d%H%M%S")
    filename_new = timestamp + '_' + filename
    shutil.move(submit_dir + filename, evaled_dir + filename_new)
    return filename_new


def exec_case(seed):
    pass


def save_result(filename, result):
    pass


def evaluate(filename):
    shutil.move(submit_dir + filename, eval_file)
    result = Parallel(n_jobs=4, verbose=5)(
        delayed(exec_case)(seed + 1) for seed in range(2000)
    )
    filename_new = move_file(eval_file)
    save_result(filename_new, result)


def check_newfile():
    """
    新しいものの方が重要であることが多いため
    新しいものから順に評価する
    """
    file_list = os.listdir(submit_dir)
    if not file_list:
        return False
    return sorted(
        file_list,
        key=lambda f: os.stat(submit_dir + f).st_mtime
    )[0]


def mainloop():
    while True:
        time.sleep(10)
        newfile = check_newfile()
        if not newfile:
            continue
        evaluate(newfile)


if __name__ == '__main__':
    mainloop()
