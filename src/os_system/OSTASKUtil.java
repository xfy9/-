package os_system;

import java.util.Scanner;

/**
 * @classname: OSTASKUtil
 * @description: TODO
 * Author : asus
 * Date : 2021/12/11 14:45
 * Version: 1.0
 **/
class OSTASKSUSPEND
{
	static final int OS_ERR_TASK_SUSPEND_IDLE=-1;
	static final int OS_TASK_IDLE_PRIO=0; //空闲任务
	static final int OS_LOWEST_PRIO=64;//最小优先级
	static final int OS_ERR_PRIO_INVALID=-2; //优先级无效
	static final int OS_ERR_TASK_SUSPEND_PRIO=-3;//错误的优先级
	static final int OS_ERR_TASK_NOT_EXIST=-4;//任务不存在
	static final int OS_ERR_TASK_NOT_SUSPENDED=-5;//没有任务挂起
	static final int OS_STAT_RDY=1;//任务处于就绪状态
	static final int OS_STAT_SUSPEND=2;//任务处于挂起状态
	static final int OS_STAT_SEM=3;//任务等待信号量
	static final int OS_STAT_PEND_OK=4;//任务被挂起
	static final int OS_STAT_PEND_ABORT=5;//任务终止
	static final int OS_STAT_FLAG=6;//
}

public class OSTASKUtil {

	//private static final int MIN_PRI_TASK=64;
	//private static final int MAX_PRI_TASK=1;
	static int os_prio_cur=0; //当前任务优先级
	static OSTCB curr_task=null;//当前任务
	static int OS_PRIO_SELF=0;//暂时代表任务优先级
	static OSTCB OS_TCB_RESERVED=null;//是否分配了信号量
	private static BitMap ready_bitmap = new BitMap("ready_bitmap");//优先级位图
	private static OSTCB[] OSTCBPrioTbl = new OSTCB[64];//优先级数组

	//任务挂起
	public static int os_suspend(int pri_task)
	{
		int status=0;
		//挂起的为空闲任务
		if(pri_task==OSTASKSUSPEND.OS_TASK_IDLE_PRIO)
		{
			System.out.println("不能挂起空闲任务");
			return OSTASKSUSPEND.OS_ERR_TASK_SUSPEND_IDLE;
		}
		//任务优先级不符合要求
		if(pri_task>=OSTASKSUSPEND.OS_LOWEST_PRIO)
		{
			if(pri_task!=OS_PRIO_SELF) {
				System.out.println("任务优先级不符合要求");
				return OSTASKSUSPEND.OS_ERR_PRIO_INVALID;
			}
		}
		//进入临界区
		os_enter_critical();
		//挂起的任务为自己
		if(pri_task==OS_PRIO_SELF)
		{
			//获取当前任务优先级
			pri_task=curr_task.os_tcb_prio;
			status=1;
		}
		//挂起的任务为自己的优先级
		else if(pri_task==curr_task.os_tcb_prio)
		{
			status=1;
		}
		else
		{
			//挂起的任务不符合要求
			status=0;
		}
		//获取当前任务TCB
		OSTCB ptcb = OSTCBPrioTbl[pri_task];
		//TCB为空
		if(ptcb==null)
		{
			//退出临界区
			os_exit_critical();
			//返回错误的优先级
			return OSTASKSUSPEND.OS_ERR_TASK_SUSPEND_PRIO;
		}
		//TCB被信号量占用
		if(ptcb==OS_TCB_RESERVED)
		{
			//退出临界区
			os_exit_critical();
			//返回任务不存在
			return OSTASKSUSPEND.OS_ERR_TASK_NOT_EXIST;
		}
		//挂起任务
		BitMapUtil.suspendTask(ready_bitmap,pri_task);
		//改变TCB状态
		ptcb.os_tcb_stat=OSTASKSUSPEND.OS_STAT_SUSPEND;
		//退出临界区
		os_exit_critical();

		if(status==1)
		{
			//进行任务调度
			os_sched();
		}
		return -1;
	}
	//任务唤醒
	public static int os_resume(int pri_task)
	{
		//任务优先级不符合要求
		if(pri_task>=OSTASKSUSPEND.OS_LOWEST_PRIO)
		{
			return OSTASKSUSPEND.OS_ERR_PRIO_INVALID;
		}
		//进入临界区
		os_enter_critical();
		//获取要唤醒的任务TCB
		OSTCB ptcb = OSTCBPrioTbl[pri_task];
		//当前TCB为空
		if(ptcb==null)
		{
			//退出临界区
			os_exit_critical();
			return OSTASKSUSPEND.OS_ERR_TASK_SUSPEND_PRIO;
		}
		//当前TCB占据信号量
		if(ptcb == OS_TCB_RESERVED)
		{
			//退出临界区
			os_exit_critical();
			return OSTASKSUSPEND.OS_ERR_TASK_NOT_EXIST;
		}
		//当前任务未处于就绪状态
		if(ptcb.os_tcb_stat!=OSTASKSUSPEND.OS_STAT_RDY)
		{
			//设置当前任务为就绪状态
			ptcb.os_tcb_stat=OSTASKSUSPEND.OS_STAT_RDY;
			//TCB任务延时为0
			if(ptcb.os_tcb_dly==0)
			{
				//退出临界区
				BitMapUtil.createTask(ready_bitmap,pri_task);
				os_exit_critical();
				os_sched();

			}
			else
			{
				os_exit_critical();
			}
			return SemUtil.OS_ERR_NONE;
		}
		else
		{
			os_exit_critical();
		}
		os_exit_critical();
		return OSTASKSUSPEND.OS_ERR_TASK_NOT_SUSPENDED;
	}
	/*public static void os_sem_pend()
	{

	}*/
	//任务调度
	public static void os_sched()
	{
		//进入临界区
		os_enter_critical();
		//获取位图中最高优先级
		int os_prio_high_rdy = BitMapUtil.findHighestPrio(ready_bitmap);

		//最高优先级不为当前优先级
		if(os_prio_high_rdy!=os_prio_cur)
		{
			//当前为最高优先级
			curr_task = OSTCBPrioTbl[os_prio_high_rdy];
		}
		System.out.println("任务调度后的优先级为: "+os_prio_high_rdy);
		//退出临界区
		os_exit_critical();
	}
	public static void os_enter_critical()
	{
		System.out.println("进入临界区");
	}
	public static void os_exit_critical()
	{
		System.out.println("退出临界区");
	}
	public static void main(String[] args) {

		OSTCB head=new OSTCB(-1,"head");

		Scanner sc = new Scanner(System.in);

		System.out.println("请输入创建的任务个数");
		int n = sc.nextInt();
		System.out.println("请依次输入任务优先级");
		for(int i=0;i<n;i++)
		{
			int x = sc.nextInt();
			OSTCBUtil.create_task(x,OSTCBPrioTbl,head,ready_bitmap);
		}
		/*OSTCBUtil.create_task(5,OSTCBPrioTbl,head,ready_bitmap);
		OSTCBUtil.create_task(6,OSTCBPrioTbl,head,ready_bitmap);
		OSTCBUtil.create_task(4,OSTCBPrioTbl,head,ready_bitmap);
		//OSTCBUtil.create_task(6,OSTCBPrioTbl,head,ready_bitmap);
		OSTCBUtil.create_task(9,OSTCBPrioTbl,head,ready_bitmap);*/
		//OSTCBUtil.create_task(1,OSTCBPrioTbl,head,ready_bitmap);
		//os_suspend(1);
		curr_task=head.os_tcb_next;
		os_prio_cur=head.os_tcb_next.os_tcb_prio;
		System.out.println("挂起当前任务");
		os_suspend(os_prio_cur);
		OSTCBUtil.traverse(head.os_tcb_next);
		System.out.println("请输入唤醒的任务");
		int x = sc.nextInt();
		//os_suspend(os_prio_cur);
		os_resume(x);
		//OSTCBUtil.traverse(head);
	}
}
