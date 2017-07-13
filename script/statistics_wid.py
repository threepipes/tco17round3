"""
bestwidから統計をとるスクリプト
"""
import os
from matplotlib import pyplot as plt
import numpy as np


def match_row(info, rows):
    for data, cond in zip(rows, info):
        if cond and cond != int(data):
            return False
    return True


def load_ondemand(path, poison=None, wine=None, strip=None, test_round=None):
    result = []
    info = [poison, wine, strip, test_round]
    with open(path) as f:
        for row in f:
            data_list = row.split(',')
            if not match_row(info, data_list):
                continue
            result.append({
                'poison': int(data_list[0]),
                'wine': int(data_list[1]),
                'strip': int(data_list[2]),
                'round': int(data_list[3]),
                'wid': int(data_list[4]),
                'prob': float(data_list[5])
            })
    return result


def plot_poison_wid_rel(y_align, strip=8, wine=2000, ylim=None, path_dir='../data/plot/'):
    """
    poison, wineを固定したとき，
    roundごとに(strip, wid)を頂点とした折れ線グラフを描く
    """

    print('plot strip:%d wine:%d' % (strip, wine))
    csv_data_list = load_ondemand(
        '../data/bestwid_poison_%2d.csv' % strip,
        strip=strip, wine=wine)
    data = [[] for _ in range(10)]
    for row in csv_data_list:
        data[row['round'] - 1].append(row)

    fig = plt.figure(figsize=(10, 8))
    ax = fig.add_subplot(111)
    ax.set_xlabel('poison')
    ax.set_ylabel(y_align)
    plt.xticks(np.arange(1, 21, 1))
    plt.grid()
    if ylim:
        plt.yticks(np.arange(ylim[0], ylim[1], (ylim[1] - ylim[0]) / 10))
        plt.ylim(ylim)

    for i in range(10):
        color = (0, 0, i * 0.1)
        x = []
        y = []
        for row in data[i]:
            x.append(row['poison'])
            y.append(row[y_align])
        ax.plot(x, y, color=color)

    plt.savefig(path_dir + 'poison_widfig_%s_w%04d_s%02d.png' % (y_align, wine, strip))
    plt.close()


def plot_poison_wid():
    path_dir = '../data/plot_poison_wid/'
    if not os.path.exists(path_dir):
        os.makedirs(path_dir)
    for strip in range(1, 21, 1):
        plot_poison_wid_rel('prob', strip=strip, ylim=(0, 1), path_dir=path_dir)


def plot_strip_wid_rel(y_align, poison=8, wine=681, ylim=None, path_dir='../data/plot/'):
    """
    poison, wineを固定したとき，
    roundごとに(strip, wid)を頂点とした折れ線グラフを描く
    """

    print('plot poison:%d wine:%d' % (poison, wine))
    csv_data_list = load_ondemand(
        '../data/bestwid_%2d.csv' % poison,
        poison=poison, wine=wine)
    data = [[] for _ in range(10)]
    for row in csv_data_list:
        data[row['round'] - 1].append(row)

    fig = plt.figure(figsize=(10, 8))
    ax = fig.add_subplot(111)
    ax.set_xlabel('strip')
    ax.set_ylabel(y_align)
    plt.xticks(np.arange(1, 21, 1))
    plt.grid()
    if ylim:
        plt.yticks(np.arange(ylim[0], ylim[1], (ylim[1] - ylim[0]) / 10))
        plt.ylim(ylim)

    for i in range(10):
        color = (0, 0, i * 0.1)
        x = []
        y = []
        for row in data[i]:
            x.append(row['strip'])
            y.append(row[y_align])
        ax.plot(x, y, color=color)

    x_l = np.arange(1, 21, 1)
    y_l = 1 - poison / (x_l + (poison - 1) * 0.57)# * (1 - poison * 0.02)
    ax.plot(x_l, y_l, color=(1, 0, 0))

    plt.savefig(path_dir + 'widfig_%s_w%04d_p%02d.png' % (y_align, wine, poison))
    plt.close()


def stat_strip_wid_rel(y_align, poison=8, wine=681):
    """
    """

    csv_data_list = load_ondemand(
        '../data/bestwid_%2d.csv' % poison,
        poison=poison, wine=wine)
    data = [[] for _ in range(10)]
    for row in csv_data_list:
        data[row['round'] - 1].append(row)

    print('poison:%d wine:%d' % (poison, wine))
    for i in range(10):
        # print('round:%d' % (i + 1))
        for row in data[i]:
            x = row['strip']
            y = row[y_align]
            if i + 1 == 10:
                print('strip:%d prob:%f' % (x, 1 - y))
                # x1yp = x * (1 - y) / poison
                # print(str(x) + ': x*(1-y)/p=' + str(x1yp))
                # print(str(x) + 'x*(1-y)/p - (1 - 0.02p)=' + str(x1yp - (1 - 0.02 * poison)))


def plot_about_wid():
    # poison = 8
    for wine in range(1000, 2100, 1000):
        for poison in range(1, 20, 1):
            path_dir = '../data/plot_apx_readj/bestwid_w%04d/' % wine
            if not os.path.exists(path_dir):
                os.makedirs(path_dir)
            # plot_strip_wid_rel('wid', poison=poison, wine=wine, path_dir=path_dir)
            plot_strip_wid_rel('prob', poison=poison, wine=wine, ylim=(0, 1), path_dir=path_dir)


def stat_about_wid():
    wine = 2000
    for poison in range(1, 21, 1):
        stat_strip_wid_rel('prob', poison=poison, wine=wine)


if __name__ == '__main__':
    plot_poison_wid()
