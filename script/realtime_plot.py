import numpy as np
import matplotlib.pyplot as plt


def pause_plot():
    fig, ax = plt.subplots(1, 1)
    x = np.arange(-np.pi, np.pi, 0.1)
    y = np.sin(x)
    # 初期化的に一度plotしなければならない
    # そのときplotしたオブジェクトを受け取る受け取る必要がある．
    # listが返ってくるので，注意
    lines, = ax.plot(x, y)

    # ここから無限にplotする
    while True:
        # plotデータの更新
        x += 0.1
        y = np.sin(x)

        # 描画データを更新するときにplot関数を使うと
        # lineオブジェクトが都度増えてしまうので，注意．
        #
        # 一番楽なのは上記で受け取ったlinesに対して
        # set_data()メソッドで描画データを更新する方法．
        lines.set_data(x, y)

        # set_data()を使うと軸とかは自動設定されないっぽいので，
        # 今回の例だとあっという間にsinカーブが描画範囲からいなくなる．
        # そのためx軸の範囲は適宜修正してやる必要がある．
        ax.set_xlim((x.min(), x.max()))

        # 一番のポイント
        # - plt.show() ブロッキングされてリアルタイムに描写できない
        # - plt.ion() + plt.draw() グラフウインドウが固まってプログラムが止まるから使えない
        # ----> plt.pause(interval) これを使う!!! 引数はsleep時間
        plt.pause(.01)


class Func:
    """
    動的にパラメータ変更可能な描画関数
    実装は子クラスに依存
    """
    def func(self, x):
        """
        numpy配列を受け取り，対応するy座標に変換して返す
        ここで使われるパラメータを変更可能にすることで，
        描画関数を外から動かすことができる
        """
        raise NotImplementedError('func must be implemented')

    def init_plot(self, ax, x):
        """
        plotを呼び出す前に必ず1度呼び出す必要がある
        今回の仕様としては，x座標は固定
        """
        self.x = x
        self.y = self.func(x)
        self.lines, = ax.plot(self.x, self.y)

    def plot(self):
        self.y = self.func(self.x)
        self.lines.set_data(self.x, self.y)

    def modify(self, s: str):
        raise NotImplementedError('modify must be implemented')


def manipulate(plot_x, plot_y, func_plot: Func, ylim=None):
    """
    引数:
    plot_x, plot_y: 描画するためのデータ
    func_plot: パラメータにより性質を変更できる描画関数
    """
    fig, ax = plt.subplots(1, 1)
    ax.plot(plot_x, plot_y, color=(0, 0, 1))
    ax.set_xlim((np.min(plot_x), np.max(plot_x)))
    func_plot.init_plot(ax, plot_x)
    if ylim:
        ax.set_ylim(ylim)

    while True:
        # 外部入力が存在する場合はfunc_plotに変更を加える TODO
        test_sigmoid(func_plot) # 仮で入力を与える
        func_plot.plot()
        plt.pause(.01)


class Sigmoid(Func):
    def __init__(self, offset_x=0, offset_y=0, gain=1, coef=1):
        self.offset_x = offset_x
        self.offset_y = offset_y
        self.gain = gain
        self.coef = coef

    def func(self, x):
        return self.offset_y + 1 /\
            ((1 + np.power(np.e, -self.gain * (x + self.offset_x))) * self.coef)

    def modify(self, s: str):
        """
        入力と変更の例
        'x+5': offset_x += 5
        'x-5': offset_x -= 5
        'y+2.2': offset_y += 2.2
        'a-10': gain -= 10
        """
        change = float(s[1:])
        if s[0] == 'x':
            self.offset_x += change
        elif s[0] == 'y':
            self.offset_y += change
        elif s[0] == 'a':
            self.gain += change
        elif s[0] == 'c':
            self.coef += change
        else:
            print('error: invalid input "%s"' % s)


def test_sigmoid(sigmoid: Sigmoid):
    sigmoid.modify('a+0.01')
    if sigmoid.gain >= 10:
        sigmoid.gain = 0


def test_manipulate():
    x = np.arange(1, 21, 1)
    y = 0.1 * x ** 2
    manipulate(x, y, Sigmoid(offset_x=-10, coef=-1), ylim=(-1, 1))


if __name__ == "__main__":
    test_manipulate()
