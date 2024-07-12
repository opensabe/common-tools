package io.github.opensabe.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 机选号工具类
 * 
 * @author Liaoxu
 * 
 */
public class RandomUtil
{
	private RandomUtil()
	{
	}

	public final static int maxBallNums = 100;

	public static String generateRandomCode(int maxValue, boolean autoCompleteZero)
	{
		Random r = new Random();
		int ball = r.nextInt(maxValue) + 1;
		if (autoCompleteZero && ball < 10)
		{
			return new StringBuilder("0").append(ball).toString();
		}
		return Integer.toString(ball);
	}

	/*
	 * 随机的号码中包含0
	 */
	public static String generateRandomCodeContanisZero(int maxValue, boolean autoCompleteZero)
	{
		maxValue = maxValue + 1;
		Random r = new Random();
		int ball = r.nextInt(maxValue);
		if (autoCompleteZero && ball < 10)
		{
			return new StringBuilder("0").append(ball).toString();
		}
		return Integer.toString(ball);
	}

	/**
	 * 
	 * @param maxValue
	 * @param autoCompleteZero
	 * @param length
	 * @param duplicateEnable
	 *            是否允许重复
	 * @return
	 */
	public static String generateRandomCodeString(int maxValue, boolean autoCompleteZero, int length,
			boolean duplicateEnable)
	{
		if (length > maxBallNums || length < 0)
			return "";
		List<String> rBallList = new ArrayList<String>();
		String selectedBall;
		for (int i = 0; i < length; i++)
		{
			selectedBall = generateRandomCode(maxValue, autoCompleteZero);
			if (!duplicateEnable && rBallList.contains(selectedBall))
			{
				i--;
			}
			else
			{
				rBallList.add(selectedBall);
			}
		}
		Collections.sort(rBallList);
		StringBuilder result = new StringBuilder("");
		for (String rball : rBallList)
		{
			result = result.append(" ").append(rball);
		}
		return result.toString().substring(1);
	}

	/*
	 * 随机一注 普通通用方法
	 */
	public static String generateRandomCodeString(int maxValue, boolean autoCompleteZero, int length,
			boolean duplicateEnable, boolean containsZero)
	{
		if (length > maxBallNums || length < 0)
			return "";
		List<String> rBallList = new ArrayList<String>();
		String selectedBall;
		for (int i = 0; i < length; i++)
		{
			if (containsZero)
				selectedBall = generateRandomCodeContanisZero(maxValue, autoCompleteZero);
			else
				selectedBall = generateRandomCode(maxValue, autoCompleteZero);
			if (!duplicateEnable && rBallList.contains(selectedBall))
			{
				i--;
			}
			else
			{
				rBallList.add(selectedBall);
			}
		}
		Collections.sort(rBallList);
		StringBuilder result = new StringBuilder("");
		for (String rball : rBallList)
		{
			result = result.append(" ").append(rball);
		}
		return result.toString().substring(1);

	}

	/**
	 * 重庆时时彩
	 * @param maxValue
	 * @param autoCompleteZero
	 * @param length
	 * @param duplicateEnable
	 * @return
	 */
	public static String generateRandomCodeStrOfSSC(int maxValue, boolean autoCompleteZero, int length,
			boolean duplicateEnable)
	{
		if (length > maxBallNums || length < 0)
			return "";
		List<String> rBallList = new ArrayList<String>();
		String selectedBall;
		Random r = new Random();
		for (int i = 0; i < length; i++)
		{

			selectedBall = Integer.toString(r.nextInt(maxValue));
			//selectedBall = generateRandomCode(maxValue, autoCompleteZero);
			if (!duplicateEnable && rBallList.contains(selectedBall))
			{
				i--;
			}
			else
			{
				rBallList.add(selectedBall);
			}
		}
		Collections.sort(rBallList);
		StringBuilder result = new StringBuilder("");
		for (String rball : rBallList)
		{
			result = result.append(" ").append(rball);
		}
		return result.toString().substring(1);
	}

	public static String generateRandomCodeStrOfDXDS(int maxValue, boolean autoCompleteZero, int length,
			boolean duplicateEnable)
	{
		if (length > maxBallNums || length < 0)
			return "";
		List<String> rBallList = new ArrayList<String>();
		String selectedBall;
		String[] DXDS =
		{ "2", "1", "5", "4" };
		for (int i = 0; i < length; i++)
		{
			selectedBall = generateRandomCode(maxValue, autoCompleteZero);
			if (Integer.parseInt(selectedBall) > 4)
			{
				i--;
				continue;
			}
			if (!duplicateEnable && rBallList.contains(selectedBall))
			{
				i--;
			}
			else
			{
				rBallList.add(DXDS[Integer.parseInt(selectedBall) - 1]);
			}
		}
		Collections.sort(rBallList);
		StringBuilder result = new StringBuilder("");
		for (String rball : rBallList)
		{
			result = result.append(" ").append(rball);
		}
		return result.toString().substring(1);
	}

	public static int getRandomBetween(int min, int max)
	{
		if (min == max)
		{
			throw new IllegalArgumentException("min shouldn't equal max!");
		}
		Random random = new Random();
		return min + random.nextInt(Math.abs(max - min));
	}

	/**
	 * 以一定概率命中
	 * 
	 * @param probability 概率值，传入30代表30%
	 * @return 命中时返回true，未命中时返回false
	 */
	public static boolean hitByProbability(int probability)
	{
		return hitByProbability(probability, new Random());
	}

	/**
	 * 以一定概率命中
	 * 
	 * @param probability 概率值，传入30代表30%
	 * @param random 随机数发生器
	 * @return 命中时返回true，未命中时返回false
	 */
	public static boolean hitByProbability(int probability, Random random)
	{
		if (probability < 0)
		{
			probability = 0;
		}
		else if (probability > 100)
		{
			probability = 100;
		}
		int rand = random.nextInt(100);
		return rand < probability;
	}

	public static void main(String[] args)
	{
		/*int x = getRandomBetween(2, 1);
		System.out.println(x);*/
		Random random = new Random();
		for (int i = 0; i < 10; i++)
		{
			boolean result = hitByProbability(20, random);
			System.out.println(result);
		}

	}
}
