package com.vteba.opencv;

/**
 * Created by yinlei on 17-3-14.
 */
public class SimilarTest {

    /**
     * 我们把两个字符串的相似度定义为：将一个字符串转换成另外一个字符串的代价（转换的方法可能不唯一），转换的代价越高则说明两个字符串的相似度越低。比如两个字符串：“SNOWY”和“SUNNY”，下面给出两种将“SNOWY”转换成“SUNNY”的方法：
     * 变换1：
     * S - N O W Y
     * S U N N - Y
     * Cost = 3 （插入U、替换O、删除W）
     * 变换2：
     * - S N O W - Y
     * S U N - - N Y
     * Cost = 5 （插入S、替换S、删除O、删除W、插入N）
     * 用d[i, j]表示source[1..i]到target[1..j]之间的最小编辑距离，则计算d[i, j]的递推关系可以这样计算出来：
     * 如果source[i] 等于target[j]，则：
     * d[i, j] = d[i, j] + 0                                               （递推式 1）
     * 如果source[i] 不等于target[j]，则根据插入、删除和替换三个策略，分别计算出使用三种策略得到的编辑距离，然后取最小的一个：
     * d[i, j] = min(d[i, j - 1] + 1，d[i - 1, j] + 1，d[i - 1, j - 1] + 1 )            （递推式 2）
     * d[i, j - 1] + 1 表示对source[i]执行插入操作后计算最小编辑距离
     * d[i - 1, j] + 1 表示对source[i]执行删除操作后计算最小编辑距离
     * d[i - 1, j - 1] + 1表示对source[i]替换成target[i]操作后计算最小编辑距离
     *
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String source = "广东省";
        String target = "广东昌";

//      i= EditDistanceChange(source,target);
        int i = editDistance(source, target);
        System.out.println("需要" + i + "次");
    }

    //动态规划法
    public static int editDistance(String source, String target) {
        char[] s = source.toCharArray();
        char[] t = target.toCharArray();
        int slen = source.length();
        int tlen = target.length();
        int d[][] = new int[slen + 1][tlen + 1];
        for (int i = 0; i <= slen; i++) {
            d[i][0] = i;
        }
        for (int i = 0; i <= tlen; i++) {
            d[0][i] = i;
        }
        for (int i = 1; i <= slen; i++) {
            for (int j = 1; j <= tlen; j++) {
                if (s[i - 1] == t[j - 1]) {
                    d[i][j] = d[i - 1][j - 1];
                } else {
                    int insert = d[i][j - 1] + 1;
                    int del = d[i - 1][j] + 1;
                    int update = d[i - 1][j - 1] + 1;
                    d[i][j] = Math.min(insert, del) > Math.min(del, update) ? Math.min(del, update) : Math.min(insert, del);
                }
            }
        }
        return d[slen][tlen];
    }


    //递归实现 --- 穷举法（枚举法）
    private static int EditDistanceChange(String source, String target) {
        if (target.length() != 0 && source.length() == 0) {
            return EditDistanceChange(source, target.substring(1)) + 1;
        } else if (target.length() == 0 && source.length() != 0) {
            return EditDistanceChange(source.substring(1), target) + 1;
        } else if (target.length() != 0 && source.length() != 0) {
//      当源字符第一个值和目标字符第一个值相同时
            if (source.charAt(0) == target.charAt(0)) {
                return EditDistanceChange(source.substring(1), target.substring(1));
            } else {
                int insert = EditDistanceChange(source.substring(1), target) + 1;
                int del = EditDistanceChange(source, target.substring(1)) + 1;
                int update = EditDistanceChange(source.substring(1), target.substring(1)) + 1;
                return Math.min(insert, del) > Math.min(del, update) ? Math.min(del, update) : Math.min(insert, del);
            }
        } else {
            return 0;
        }
    }
}
