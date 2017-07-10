import time
import os
import shutil
from subprocess import Popen, PIPE, TimeoutExpired
from datetime import datetime

import notification
from joblib import Parallel, delayed


project_name = 'tco17_3'

working_dir = os.getenv('DATA_PATH') + project_name + '/'
submit_dir = working_dir + 'submit/'
eval_dir = working_dir + 'tmp/'
eval_file = eval_dir + 'tmp.jar' # jar前提
evaled_dir = working_dir + 'eval/'
result_dir = working_dir + 'result/'


def init():
    for d in [submit_dir, eval_dir, evaled_dir, result_dir]:
        if not os.path.exists(d):
            os.mkdir(d)


def move_file(filename):
    """
    評価後のファイルを物置に移動
    """
    timestamp = datetime.now().strftime("%m%d%H%M%S")
    filename_new = timestamp + '_' + filename
    shutil.move(submit_dir + filename, evaled_dir + filename_new)
    return filename_new


def exec_case(seed):
    """
    与えられたseedでtmp.jarを実行し，scoreと実行時間を保存
    TLEの場合は100secで打ち切り
    """
    command = ('java -jar tmp.jar -seed %d' % seed).split()
    try:
        timesec = time.time()
        p = Popen(command, cwd=eval_dir, stdout=PIPE, stderr=PIPE)
        stdout, stderr = p.communicate(timeout=100)
        timesec = time.time() - timesec
        # stdoutはスコアのみ出力するよう修正しておくこと
        result = {
            'seed': seed,
            'score': float(stdout.decode('utf-8').strip()),
            'time': timesec
        }
    except TimeoutExpired:
        result = {
            'seed': seed,
            'score': -1.0,
            'time': 100.0
        }
    return result


def save_result(filename, result):
    filename = '_result.csv'
    with open(result_dir + filename, 'w') as f:
        for row in result:
            f.write(','.join(
                [result[col] for col in ['seed', 'score', 'time']]
            ) + '\n')


def evaluate(filename):
    """
    slackに開始通知
    tmpディレクトリで並列評価
    結果の保存
    """
    notification.slack('begin evaluation of ' + filename)
    shutil.move(submit_dir + filename, eval_file)
    result = Parallel(n_jobs=4, verbose=5)(
        delayed(exec_case)(seed + 1) for seed in range(2000)
    )
    filename_new = move_file(eval_file)
    save_result(filename_new, result)


def check_newfile():
    """
    新しいものの方が重要であることが多いため
    新しいものから順に評価する(直感的でないので注意)
    """
    file_list = os.listdir(submit_dir)
    if not file_list:
        return False
    return sorted(
        file_list,
        key=lambda f: os.stat(submit_dir + f).st_mtime
    )[0]


def mainloop():
    """
    評価キュー(submitディレクトリ)に新しいファイルがあれば評価
    評価に結構時間がかかる 1度に行う評価は単一のため，
    監視ツールとかは使っていない
    """
    while True:
        time.sleep(10)
        newfile = check_newfile()
        if not newfile:
            continue
        evaluate(newfile)


if __name__ == '__main__':
    init()
    mainloop()
