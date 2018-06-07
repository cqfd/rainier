package com.stripe.rainier.core

import com.stripe.rainier.compute.Real

/**
  * Basic probability distribution trait
  */
trait Distribution[T] extends Likelihood[T] { self =>
  def logDensity(t: T): Real
  def logDensities(list: Seq[T]): Real = Real.sum(list.map(logDensity))

  def generator: Generator[T]

  def fit(t: T): RandomVariable[Distribution[T]] =
    RandomVariable(this, logDensity(t))
  def fit(list: Seq[T]): RandomVariable[Distribution[T]] =
    RandomVariable(this, logDensities(list))
}

/**
  * Combinatoric functions required for log density calculations. Note that they all return the log of the function described.
  */
object Combinatrics {
  def gamma(z: Real): Real = {
    val w = z + (Real.one / ((12 * z) - (Real.one / (10 * z))))
    (Real(Math.PI * 2).log / 2) - (z.log / 2) + (z * (w.log - 1))
  }

  def beta(a: Real, b: Real): Real =
    gamma(a) + gamma(b) - gamma(a + b)

  def factorial(k: Int): Real = gamma(Real(k + 1))

  def choose(n: Int, k: Int): Real =
    factorial(n) - factorial(k) - factorial(n - k)
}
