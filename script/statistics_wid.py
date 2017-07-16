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


def check_border(strip=1, wine=2000):
    """
    widが最後に90になるときのpoisonを求める
    """

    print('stat strip:%d wine:%d' % (strip, wine))
    csv_data_list = load_ondemand(
        '../data/bestwid_poison.csv',
        strip=strip, wine=wine)
    data = [[] for _ in range(10)]
    for row in csv_data_list:
        data[row['round'] - 1].append(row)

    border = []
    for i in range(10):
        x_last = -1
        y_pre = -1
        y_nx = -1
        for j, row in enumerate(data[i][:-1]):
            x = row['poison']
            y = row['wid']
            y_next = data[i][j + 1]['wid']
            if y >= 90 and y_next <= 90:
                x_last = x
                y_pre = y
                y_nx = y_next
        per = - (90 - y_pre) / (y_pre - y_nx)
        print('round:%2d x:%f' % (i + 1, x_last + per))
        border.append(x_last + per)
    return border


def border_diffs():
    border_list = []
    for i in range(20):
        border_list.append(check_border(strip=(i + 1)))

    x = np.arange(1, 11, 1)
    borders = [[] for _ in range(10)]
    for i in range(len(border_list) - 1):
        s = "%2d -> %2d: " % (i, i + 1)
        for j, (p1, p2) in enumerate(zip(border_list[i], border_list[i + 1])):
            s += "%2.6f " % (p2 - p1)
            borders[j].append(p2 - p1)
        print(s)

    fig = plt.figure(figsize=(8, 8))
    ax = fig.add_subplot(111)
    ax.set_xlabel('round')
    ax.set_ylabel('strip(wid=90)')
    plt.xticks(np.arange(1, 20, 1))
    plt.grid()
    plt.yticks(np.arange(0, 20, 1))

    for i, border_y in enumerate(border_list):
        y = np.array(border_y)
        ax.plot(x, y, color=(0, i / 20, 0))

    freq = 3
    for i in range(freq):
        y_adj = 21 * 2 / (x + 1)# + (0.46 / (i + 1)) * np.log10(x)
        ax.plot(x, y_adj, color=((i + 1) / (freq + 1), 0, 0))

    plt.savefig('../data/border_01_adj.png')
    plt.close()

    print('all:')
    diff = []
    s = ''
    for p1, p2 in zip(border_list[0], border_list[-1]):
        diff.append((p2 - p1) / 19)
        s += "%2.6f " % ((p2 - p1) / 19)
    print(s)
    for i in range(len(diff) - 1):
        print(diff[i + 1] - diff[i])
    # plt.ylim((0, 10))
    # plt.xlim((0, 10))
    # plt.xticks(np.arange(1, 11, 1))
    # plt.yticks(np.arange(0, 0.5, 0.05))

    # ax.plot(x, diff)
    # for i in range(3):
    # ax.plot(x, 0.5 - 1 / (x + 1), color=(1, 0, 0))
    # ax.plot(x, np.log10(x) * 0.46, color=(0, 1, 0))
    # ax.plot(x, np.log2(x) * 0.14, color=(0, 0, 0))
    # plt.savefig('../data/border_02_adj.png')
    # plt.close()



def stat_poison_wid_rel(y_align, strip=8, wine=2000):
    """
    strip, wineを固定したとき，
    roundごとに(strip, wid)を頂点とした折れ線グラフを描く
    """

    print('stat strip:%d wine:%d' % (strip, wine))
    csv_data_list = load_ondemand(
        '../data/bestwid_poison.csv',
        strip=strip, wine=wine)
    data = [[] for _ in range(10)]
    for row in csv_data_list:
        data[row['round'] - 1].append(row)

    for i in range(10):
        xy = []
        for row in data[i]:
            x = row['poison']
            y = row[y_align]
            print('round:%2d p:%d w:%d wid*poison=%d' % (i + 1, x, y, x * y))
            if x >= 10:
                xy.append(x * y)
        print('med: ', np.median(xy))


def plot_poison_wid_rel(y_align, strip=8, wine=2000, ylim=None, path_dir='../data/plot/'):
    """
    strip, wineを固定したとき，
    roundごとに(strip, wid)を頂点とした折れ線グラフを描く
    """

    print('plot strip:%d wine:%d' % (strip, wine))
    csv_data_list = load_ondemand(
        '../data/bestwid_poison.csv',
        strip=strip, wine=wine)
    data = [[] for _ in range(10)]
    for row in csv_data_list:
        data[row['round'] - 1].append(row)

    fig = plt.figure(figsize=(10, 8))
    ax = fig.add_subplot(111)
    ax.set_xlabel('poison')
    ax.set_ylabel(y_align)
    plt.xticks(np.arange(0, 101, 5))
    plt.grid()
    if ylim:
        plt.yticks(np.arange(ylim[0], ylim[1], (ylim[1] - ylim[0]) / 20))
        plt.ylim(ylim)

    for i in range(10):
        color = (0, 0, i * 0.1)
        x = []
        y = []
        for row in data[i]:
            x.append(row['poison'])
            y.append(row[y_align])
        ax.plot(x, y, color=color)

    for i in range(10):
        r = i + 1
        color = ((10 - i) * 0.1, 0, 0)
        x = np.arange(1, 101, 1)
        y = 2 * wine / ((x - 0.46 * np.log10(r) * (strip - 3)) * (r*1.1 + 1))
        ax.plot(x, y, color=color)

    plt.savefig(path_dir + 'poison_widfig_%s_w%04d_s%02d.png' % (y_align, wine, strip))
    plt.close()


def plot_poison_wid():
    path_dir = '../data/plot_poison_wid_wna/'
    if not os.path.exists(path_dir):
        os.makedirs(path_dir)
    for strip in range(1, 21, 1):
        plot_poison_wid_rel('wid', strip=strip, ylim=(0, 100), path_dir=path_dir)


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
