package com.vteba.opencv;

import java.util.*;

/**
 * 常用的统计方法
 *
 * @author yinlei
 * @since 2017-3-8
 */
public class MathUtils {

    /**
     * 对一组integer数组求平均值
     *
     * @param is
     * @return 返回平均值
     */
    public static Integer mean(Integer... is) {
        if (is == null) {
            return 0;
        }
        int size = is.length;
        if (size == 0) {
            return 0;
        } else if (size == 1) {
            return is[0];
        }

        int temp = 0;
        for (Integer i : is) {
            temp += i;
        }

        return temp / size;
    }

    /**
     * 对一组integer数组求平均值。mean method的同义词。
     *
     * @param is
     * @return 平均值
     */
    public static Integer avg(Integer... is) {
        return mean(is);
    }

    /**
     * 求collection的平均值
     *
     * @param list
     * @return 平均值
     */
    public static Integer mean(Collection<Integer> list) {
        if (list == null) {
            return 0;
        }
        int size = list.size();
        if (size == 0) {
            return 0;
        } else if (size == 1) {
            return list.stream().findFirst().get();
        }
        Integer[] is = new Integer[size];
        return mean(list.toArray(is));
    }

    public double findMedianSortedArrays(int A[], int B[]) {
        double median;
        int n = A.length;
        int m = B.length;
        if ((n + m) % 2 == 0) {
            int t1 = getTopK(A, 0, n - 1, B, 0, m - 1, (m + n) / 2);
            int t2 = getTopK(A, 0, n - 1, B, 0, m - 1, (m + n + 2) / 2);
            median = (t1 + t2) * 1.0 / 2;
        } else {
            median = getTopK(A, 0, n - 1, B, 0, m - 1, (m + n + 1) / 2);
        }
        return median;
    }

    private static int getTopK(int[] arrA, int sa, int ea, int[] arrB, int sb,
                               int eb, int k) {
        int ma = (sa + ea) / 2;
        int mb = (sb + eb) / 2;
        if (sa > ea) {
            return arrB[sb + k - 1];
        }
        if (sb > eb) {
            return arrA[sa + k - 1];
        }
        if (arrA[ma] >= arrB[mb]) {
            if ((ma - sa + 1) + (mb - sb + 1) > k) {
                return getTopK(arrA, sa, ma - 1, arrB, sb, eb, k);
            } else {
                return getTopK(arrA, sa, ea, arrB, mb + 1, eb, k - (mb + 1 - sb));
            }
        } else {
            if ((ma - sa + 1) + (mb - sb + 1) > k) {
                return getTopK(arrA, sa, ea, arrB, sb, mb - 1, k);
            } else {
                return getTopK(arrA, ma + 1, ea, arrB, sb, eb, k - (ma + 1 - sa));
            }
        }
    }

    /**
     * 求数组的中位数，应该用double，否则会四舍五入
     *
     * @param is
     * @return 中位数
     */
    public static Integer media(Integer... is) {
        if (is == null) {
            return null;
        }
        int length = is.length;
        if (length == 0) {
            return null;
        } else if (length == 1) {
            return is[0];
        } else if (length == 2) {
            return (is[0] + is[1]) / 2;
        }

        Arrays.sort(is);
        int l = length / 2;
        if (length % 2 == 0) {
            return (is[l] + is[l + 1]) / 2;
        } else {
            return is[l];
        }
    }

    /**
     * 求中位数
     *
     * @param is
     * @return
     */
    public static Integer media(Collection<Integer> is) {
        if (is == null) {
            return null;
        }
        int length = is.size();
        if (length == 0) {
            return null;
        } else if (length == 1) {
            return is.stream().findFirst().get();
        } else if (length == 2) {
            return mean(is); // 2个就是求平均值
        }

        List<Integer> list = new ArrayList<>(is);
        Collections.sort(list);
        int l = length / 2;
        if (length % 2 == 0) {
            return (list.get(l) + list.get(l + 1)) / 2;
        } else {
            return list.get(l);
        }
    }

    public static void main(String[] args) {

        Integer[] arr2 = {3, 43, 545, 23, 12, 7, 44, 233, 234, 30};

        System.out.println(media(arr2));

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        Integer[] is = new Integer[1];

        Integer[] ints = list.toArray(is);
        System.out.println(is);
    }


}
