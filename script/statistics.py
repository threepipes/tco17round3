"""
雑多な統計を取るための簡易スクリプト
"""

import numpy as np
import matplotlib.pyplot as plt

data_path = '../data/'
file_name = 'result.csv'

def histogram(x, bins, xlim=None):
    fig = plt.figure()
    ax = fig.add_subplot(1,1,1)

    ax.hist(x, bins=bins)
    ax.set_xlabel('x')
    ax.set_ylabel('freq')
    # fig.show()
    if xlim:
        ax.set_xlim(xlim)

    plt.show()


def stat_poison_dist():
    """
    毒の数の分布を計測
    """
    x = []
    for wine in range(50, 10001):
        for poison in range(1, wine // 50 + 1):
            x.append(poison)
    histogram(x, 100)


def load_max():
    x = [0] * 2000
    for i in range(2, 10):
        _file_name = 'result_0.%d0.csv' % i
        data = []
        with open(data_path + _file_name) as f:
            for row in f:
                data.append(row.split(',')[1].strip())
        for i, d in enumerate(data[1:]):
            x[i] = max(x[i], float(d))
    return x


def load_max_2():
    x = []
    with open(data_path + 'resultset.csv') as f:
        for row in f:
            x.append(max(map(float, row.strip().split(','))))
    return x


def load(_file_name):
    x = []
    with open(data_path + _file_name) as f:
        for row in f:
            x.append(row.split(',')[1].strip())
    x = list(map(float, x[1:]))
    return x


def stat_point_dist():
    """
    得点分布を計算
    """
    x = load(file_name)

    x = np.array(x)
    print('mean', np.mean(x))
    print('med', np.median(x))
    print('mean < x', len(x[x > np.mean(x)]))

    histogram(x, 500, xlim=(0, 0.2))


def stat_point_weight():
    """
    得点の累積和グラフ
    全体の得点に対して，何点くらいのケースが鍵になってるのか知りたかった
    """
    x = [0] + load(file_name)
    y = sorted(x)
    x = sorted(x)
    for i in range(1, len(y)):
        y[i] += y[i - 1]
    plt.plot(x, y)

    x = [0] + load_max_2()
    y = sorted(x)
    x = sorted(x)
    for i in range(1, len(y)):
        y[i] += y[i - 1]
    plt.plot(x, y)
    plt.show()


if __name__ == '__main__':
    stat_point_weight()
