# -*- coding: utf-8 -*-
"""
Compute correlation between values and their indexes in the stream.
"""
import math


class Correlator:
    """
    Compute correlation between values and their indexes in the stream.
    """
    def __init__(self):
        self.n: int = 0
        """The number of points encountered so far in the stream"""
        self.mx: float = 0.0
        """mean of x's, in this case the indexes"""
        self.my: float = 0.0
        """mean of y's"""
        self.vx: float = 0.0
        """ variance of x's"""
        self.vy: float = 0.0
        """variance of y's"""
        self.cxy: float = 0.0
        """covariance of x and y"""

    def add_point(self, val: float):
        """Append a new value to the stream"""
        self.n += 1
        f = 1.0 / self.n
        dx = self.n - self.mx
        dy = val - self.my
        self.mx += f * dx
        self.my += f * dy
        self.vx = (1.0 - f) * (self.vx + f * dx * dx)
        self.vy = (1.0 - f) * (self.vy + f * dy * dy)
        self.cxy = (1.0 - f) * (self.cxy + f * dx * dy)

    def approx_correlation(self):
        """Return the current correlation for the stream"""
        try:
            return self.cxy / math.sqrt(self.vx * self.vy)
        except:
            return 0.0


if __name__ == '__main__':
    c = Correlator()
    for y in range(10):
        c.add_point(y)
    print('Should be 1:', c.approx_correlation())

    for y in range(10):
        c.add_point(y * y)
    print('Should be positive:', c.approx_correlation())

    c = Correlator()
    for y in range(10):
        c.add_point(-3 * y)
    print('Should be -1:', c.approx_correlation())

    c = Correlator()
    for y in range(10):
        c.add_point(-y * y)
    print('Should be negative:', c.approx_correlation())

    c = Correlator()
    for y in range(10):
        c.add_point(1)
    print('Should be 0:', c.approx_correlation())
