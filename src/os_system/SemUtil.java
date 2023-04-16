package os_system;

import java.util.Scanner;

/**
 * @classname: SemUtil
 * @description: TODO
 * Author : asus
 * Date : 2021/12/18 10:07
 * Version: 1.0
 **/
public class SemUtil {

	// error
	final static int OS_NO_ERR=5; //信号量不为零
	final static int OS_ERR_PEVENT_NULL=1; //pevent 为空
	final static int OS_ERR_EVENT_TYPE=2;//pevent 不是指向信号量的指针
	final static int OS_ERR_PEND_ISR=3; //从中断调用该函数。虽然规定了不允许从中断调用该函数，但μC/OS-Ⅱ仍然包含了检测这种情况的功能。
	final static int OS_ERR_PEND_LOCKED=4;//上锁不能进行调度
	final static int OS_ERR_NONE=5; //调用成功
	final static int OS_ERR_PEND_ABORT=6; //对信号量的等待被中止
	final static int OS_ERR_TIMEOUT=7; //超时


	private final static int OS_EVENT_TYPE_SEM=1;

	private static int OSIntNesting=0; //记录中断嵌套的层数
	private static int OSLockNesting=0;//调度锁



	//请求信号量
	public static void os_sem_pend(OSEvent pevent, int timeout, int perr,OSTCB curr_tcb)
	{
		//错误码为0
		if(perr==0)
		{
			//内核创建过程发生异常
			os_safety_critical_exception();
			return;
		}
		//信号量指针为空
		if(pevent==null)
		{
			perr=OS_ERR_PEVENT_NULL;
			return;
		}
		//pevent不是指向信号量的指针
		if(pevent.os_event_type!=OS_EVENT_TYPE_SEM)
		{
			perr = OS_ERR_EVENT_TYPE;
		}
		//中断嵌套的层数大于0
		if(OSIntNesting>0)
		{
			perr=OS_ERR_PEND_ISR;
			return;
		}
		//调度锁
		if(OSLockNesting>0)
		{
			//上锁不能进行调度
			perr=OS_ERR_PEND_LOCKED;
			return;
		}
		//进入临界区
		OSTASKUtil.os_enter_critical();
		//事件信号量大于0
		if(pevent.os_event_cnt>0)
		{
			//信号量减一，进行信号量与任务调度
			pevent.os_event_cnt--;
			//退出临界区
			OSTASKUtil.os_exit_critical();
			//调用成功
			System.out.println("信号量获取成功");
			perr=OS_NO_ERR;
			return;
		}
		//进入等待队列
		curr_tcb.os_tcb_stat=OSTASKSUSPEND.OS_STAT_SEM;
		curr_tcb.os_tcb_statpend=OSTASKSUSPEND.OS_STAT_PEND_OK;
		//设置任务延迟时间
		curr_tcb.os_tcb_dly=timeout;
		//OS_EventTaskWait(pevent);
		//退出临界区
		OSTASKUtil.os_exit_critical();
		//进行任务调度
		OSTASKUtil.os_sched();
		//进入临界区
		OSTASKUtil.os_enter_critical();
		//任务为挂起状态
		if(curr_tcb.os_tcb_statpend==OSTASKSUSPEND.OS_STAT_PEND_OK)
		{
			perr=OS_ERR_NONE;
		}
		//任务为终止状态
		else if(curr_tcb.os_tcb_statpend==OSTASKSUSPEND.OS_STAT_PEND_ABORT)
		{
			perr=OS_ERR_PEND_ABORT;
		}
		//任务超时
		else
		{
			perr=OS_ERR_TIMEOUT;
		}
		//设置任务为就绪状态
		curr_tcb.os_tcb_stat=OSTASKSUSPEND.OS_STAT_RDY;
		curr_tcb.os_tcb_statpend=OSTASKSUSPEND.OS_STAT_PEND_OK;
		curr_tcb.os_tcb_event_ptr=null;
		curr_tcb.os_tcb_event_multi_rdy=null;
		curr_tcb.os_tcb_event_multi_ptr=null;
		//退出临界区
		OSTASKUtil.os_exit_critical();
	}
	public static void os_safety_critical_exception()
	{
		System.out.println("内核创建过程发生异常");
	}

	public static void main(String[] args) {

		OSTCB head=new OSTCB(-1,"head");
		BitMap bitMap = new BitMap("bitMap");
		OSTCB[] OSTCBPrioTbl = new OSTCB[64];//优先级数组
		int perr = OS_NO_ERR;
		Scanner sc = new Scanner(System.in);
		System.out.println("请输入任务优先级");
		int x = sc.nextInt();
		OSTCBUtil.create_task(x,OSTCBPrioTbl,head,bitMap);
		//OSTCB ostcb = new OSTCB(5,"test");
		OSEvent event = new OSEvent(1,1,5,head.os_tcb_next,bitMap);
		os_sem_pend(event,5,perr,head.os_tcb_next);
	}
}

class OSEvent
{
	int os_event_type;
	int os_event_grp;
	int os_event_cnt; //
	OSTCB os_event_ptr;
	BitMap bitMap;
	public OSEvent(int os_event_type, int os_event_grp, int os_event_cnt,
				   OSTCB os_event_ptr, BitMap bitMap)
	{
		this.os_event_type=os_event_type;
		this.os_event_grp=os_event_grp;
		this.os_event_cnt=os_event_cnt;
		this.os_event_ptr=os_event_ptr;
		this.bitMap=bitMap;
	}


}