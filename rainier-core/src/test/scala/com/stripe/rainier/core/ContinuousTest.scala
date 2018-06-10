package com.stripe.rainier.core

import com.stripe.rainier.compute._
import com.stripe.rainier.sampler._
import org.scalatest.FunSuite

class ContinuousTest extends FunSuite {
  implicit val rng: RNG = ScalaRNG(1528645983605L)

  def check(description: String)(fn: Real => Continuous): Unit = {
    println(description)
    List((Walkers(100), 10000), (HMC(5), 1000)).foreach {
      case (sampler, iterations) =>
        println((sampler, iterations))
        List(0.1, 1.0, 2.0).foreach { trueValue =>
          val trueDist = fn(Real(trueValue))
          val syntheticData =
            RandomVariable(trueDist.generator).sample().take(1000)
          val sampledData =
            trueDist.param.sample(sampler, iterations, iterations)
          val model =
            for {
              x <- LogNormal(0, 1).param
              _ <- fn(x).fit(syntheticData)
            } yield x
          val fitValues = model.sample(sampler, iterations, iterations)

          val syntheticMean = syntheticData.sum / syntheticData.size
          val syntheticStdDev = Math.sqrt(syntheticData.map { n =>
            Math.pow(n - syntheticMean, 2)
          }.sum / syntheticData.size)
          val sampledMean = sampledData.sum / sampledData.size
          val yErr = (sampledMean - syntheticMean) / syntheticStdDev

          val fitMean = fitValues.sum / fitValues.size
          val xErr = (fitMean - trueValue) / trueValue

          test(
            s"y ~ $description, x = $trueValue, sampler = $sampler, E(y) within 0.2 SD") {
            assert(yErr.abs < 0.2)
          }

          test(
            s"y ~ $description, x = $trueValue, sampler = $sampler, E(x) within 5%") {
            assert(xErr.abs < 0.05)
          }
        }
    }
  }

  check("Normal(x,x)") { x =>
    Normal(x, x)
  }

  check("LogNormal(x,x)") { x =>
    LogNormal(x, x)
  }

  check("Exponential(x)") { x =>
    Exponential(x)
  }
  /*
  check("Uniform(x,x*2)") { x =>
    Uniform(x, x * 2)
  }
   */
  check("Laplace(x,x)") { x =>
    Laplace(x, x)
  }
}
