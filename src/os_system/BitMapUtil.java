package os_system;

import java.util.Scanner;

/**
 * @classname: BitMapUtil
 * @description: TODO
 * Author : asus
 * Date : 2021/12/9 16:03
 * Version: 1.0
 **/
public class BitMapUtil {
	//最小优先级
	private  static  final int MIN_TASK_PRI=63;
	//最大优先级
	private  static  final int MAX_TASK_PRI=0;
	//创建任务
	public static boolean createTask(BitMap bitMap,int pri_grade)
	{
		//如果任务的优先级不符合要求，返回创建任务失败信息
		if(pri_grade>MIN_TASK_PRI||pri_grade<MAX_TASK_PRI)
		{
			System.out.println("任务的优先级不符合要求");
			return false;
		}
		//获取任务优先级位于位图的什么位置
		int row = pri_grade>>3;
		//获取任务优先级所在第几位
		int col = pri_grade&((1<<3)-1);

		//如果任务优先级已有任务占用，返回创建任务失败信息
		if((bitMap.priority[row]&(1<<col))>0)
		{
			System.out.println("已有任务设置此优先级");
			return false;
		}

		//位图任务优先级位数设为1 返回设置优先级成功信息
		bitMap.priority[row] = (byte) (bitMap.priority[row]|(1<<(col)));
		System.out.println("设置优先级成功");
		return true;
	}

	//挂起任务
	public static boolean suspendTask(BitMap bitMap,int pri_grade)
	{
		//如果要挂起的任务优先级不符合要求，返回错误信息
		if(pri_grade>MIN_TASK_PRI||pri_grade<MAX_TASK_PRI)
		{
			System.out.println("任务的优先级不符合要求");
			return false;
		}
		//找到要挂起的任务优先级所在的数组位置
		int row = pri_grade>>3;
		//找到要挂起的任务优先级所在的位数
		int col = pri_grade&((1<<3)-1);

		//如果要挂起任务优先级在位图的位数为0，返回挂起失败信息
		if((bitMap.priority[row]&(1<<col))==0)
		{
			System.out.println("设置为0失败，因为此优先级已经为0");
			return false;
		}
		//设置任务优先级在位图中的位数为0 返回挂起成功信息
		bitMap.priority[row] = (byte) (bitMap.priority[row]&(~(1<<(col))));
		System.out.println("设置成功，此优先级成功设置为0");
		return true;
	}
	//获取任务优先级状态
	public static int get_pri_status(BitMap bitMap,int pri_grade)
	{
		//如果要挂起的任务优先级不符合要求，返回错误信息
		if(pri_grade>MIN_TASK_PRI||pri_grade<MAX_TASK_PRI)
		{
			System.out.println("任务的优先级不符合要求");
			return -1;
		}
		//找到要挂起的任务优先级所在的数组位置
		int row = pri_grade>>3;
		//找到要挂起的任务优先级所在的位数
		int col = pri_grade&((1<<3)-1);
		//返回此任务优先级状态
		return bitMap.priority[row]&(1<<col);
	}
	//获取位图中优先级最高的任务
	public static int findHighestPrio(BitMap bitMap)
	{
		int i=0;
		//找到第一个不为0的位图元素
		while(i<bitMap.priority.length)
		{
			if(bitMap.priority[i]!=0) break;
			i++;
		}
		//如果位图中的任务优先级全为0，则没有最高的任务优先级
		if(i==bitMap.priority.length)
		{
			System.out.println("无最高优先级");
			return -1;
		}
		//找到最低位
		int x = bitMap.priority[i]&(~bitMap.priority[i]+1);
		int low_bit=0;
		//找到最低位所在的位置
		while(x>0)
		{
			x>>=1;
			low_bit++;
		}
		//返回位图中任务优先级最高的优先级
		return i*8+low_bit-1;
	}
	//获取位图状态
	public static void get_task_map_status(BitMap bitMap)
	{
		for(int i=0;i<bitMap.priority.length;i++)
		{
			String s = Integer.toBinaryString(((bitMap.priority[i]) & 0xFF) + 0x100).substring(1);
			System.out.println(s);
		}
	}
	public static void main(String[] args) {

		BitMap bitMap = new BitMap("task_bitmap");
		int num=16;
		Scanner sc = new Scanner(System.in);
		System.out.println("请输入创建的任务数目");
		int n = sc.nextInt();
		//int[] tasks = new int[]{1,8,9,10,11,12,13,14,15,63,0,7};
		System.out.println("请依次输入任务优先级");
		for (int i=0;i<n;i++)
		{
			int pri = sc.nextInt();
			createTask(bitMap,pri);
			/*for(int j=1;j<2;j++) {
				System.out.println(bitMap.priority[j]);
			}*/
			//System.out.println("-----------------");
		}
		System.out.println("请输入要挂起的任务");
		suspendTask(bitMap,sc.nextInt());
		//System.out.println("任务最高优先级为: "+findHighestPrio(bitMap));
		get_task_map_status(bitMap);
		/*for(int i=0;i<tasks.length;i++)
		{
			suspendTask(bitMap,tasks[i]);
			for(int j=1;j<2;j++) {
				System.out.println(bitMap.priority[j]);
			}
		}*/

	}
}
//位图数据结构
class BitMap{
	//任务优先级数组
	byte[] priority = new byte[8];
	//优先级名称
	String bitmap_name;
	public BitMap(String bitmap_name)
	{
		this.bitmap_name=bitmap_name;
	}
}
