package os_system;

import java.util.Scanner;

/**
 * @classname: OSTimeDlyUtil
 * @description: TODO
 * Author : asus
 * Date : 2021/12/29 18:02
 * Version: 1.0
 **/
public class OSTimeDlyUtil {
	private static int OSIntNesting=0; //记录中断嵌套的层数
	private static int OSLockNesting=0;//调度锁
	static OSTCB curr_task=null;//当前任务
	private static BitMap ready_bitmap = new BitMap("ready_bitmap");//优先级位图
	private static OSTCB[] OSTCBPrioTbl = new OSTCB[64];//优先级数组

	//任务调度
	public static void os_sched()
	{
		//进入临界区
		OSTASKUtil.os_enter_critical();
		//获取位图中最高优先级
		int os_prio_high_rdy = BitMapUtil.findHighestPrio(ready_bitmap);

		//当前为最高优先级
		curr_task = OSTCBPrioTbl[os_prio_high_rdy];

		System.out.println("任务调度后的优先级为: "+os_prio_high_rdy);
		//退出临界区
		OSTASKUtil.os_exit_critical();
	}
	//任务延时
	public static void osTimeDly(int ticks //延时时间
	)
	{
		//是否存在中断
		if(OSIntNesting>0)
		{
			return;
		}
		//是否存在调度锁
		if(OSLockNesting>0)
		{
			return;
		}
		//判断延时时间是否大于0
		if(ticks>0)
		{
			//进入临界区
			OSTASKUtil.os_enter_critical();
			//获取当前任务优先级
			int pri = curr_task.os_tcb_prio;
			//挂起当前任务
			BitMapUtil.suspendTask(ready_bitmap,pri);
			OSTCBPrioTbl[pri]=null;
			//设置当前任务时延时间
			curr_task.os_tcb_dly=ticks;
			//退出临界区
			OSTASKUtil.os_exit_critical();
			//执行任务调度
			os_sched();
		}
	}

	public static void main(String[] args) {
		OSTCB head=new OSTCB(-1,"head");
		Scanner sc = new Scanner(System.in);
		System.out.print("请输入创建任务数目: ");
		int num = sc.nextInt();
		System.out.println("请依次输入任务优先级数目");
		for(int i=0;i<num;i++)
		{
			int x = sc.nextInt();
			OSTCBUtil.create_task(x,OSTCBPrioTbl,head,ready_bitmap);
		}
		curr_task = head.os_tcb_next;
		System.out.println("正在运行任务优先级为: "+curr_task.os_tcb_prio);
		System.out.println("位图信息为:");
		BitMapUtil.get_task_map_status(ready_bitmap);
		System.out.println("请输入任务延时时间");
		int ticks = sc.nextInt();
		osTimeDly(ticks);
		System.out.println("执行任务延时后位图信息");
		BitMapUtil.get_task_map_status(ready_bitmap);
	}
}