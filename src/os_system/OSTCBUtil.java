package os_system;

import javax.sound.midi.Soundbank;

/**
 * @classname: OSTCB
 * @description: TODO
 * Author : asus
 * Date : 2021/12/13 19:40
 * Version: 1.0
 **/
class OSTCB {
	OSTCB os_tcb_next;  //指向后一个任务
	OSTCB os_tcb_prev; //进程前一个任务
	int os_tcb_prio; //任务优先级
	int os_tcb_statpend; //任务挂起标志
	int os_tcb_stat; //任务启动标志
	int os_tcb_dly;  //任务延时
	String task_name; //任务名称
	OSEvent os_tcb_event_ptr;
	OSEvent os_tcb_event_multi_ptr;
	OSEvent os_tcb_event_multi_rdy;
	OSFlagNode OSTCBFlagNode;

	public  OSTCB(int os_tcb_prio,String task_name)
	{
		this.os_tcb_prio=os_tcb_prio;
		this.task_name=task_name;
		this.os_tcb_prev=this.os_tcb_next=null;
		this.os_tcb_statpend=this.os_tcb_stat=0;
		this.os_tcb_event_ptr=null;
		this.os_tcb_event_multi_ptr=null;
		this.os_tcb_event_multi_rdy=null;
	}
}
public class OSTCBUtil{
	private  static  final int MIN_TASK_PRI=63;
	private  static  final int MAX_TASK_PRI=0;
	public static boolean create_task(int prio,OSTCB[] OSTCBPrioTbl,OSTCB ostcb_list,BitMap ready_bitmap)
	{
		if(prio<MAX_TASK_PRI||prio>MIN_TASK_PRI)
		{
			System.out.println("优先级不符合规则");
			return false;
		}
		if(OSTCBPrioTbl[prio]!=null)
		{
			System.out.println("已有任务占用此优先级");
			return true;
		}
		BitMapUtil.createTask(ready_bitmap,prio);
		OSTCB ostcb = new OSTCB(prio,"TASK"+String.valueOf(prio));
		OSTCBPrioTbl[prio]=ostcb;
		OSTCB temp_ostcb = ostcb_list;
		OSTCB pre_ostcb = null;
		while(temp_ostcb!=null&&temp_ostcb.os_tcb_prio<prio)
		{
			pre_ostcb=temp_ostcb;
			temp_ostcb=temp_ostcb.os_tcb_next;
		}
		if(temp_ostcb!=null)
		{
			pre_ostcb.os_tcb_next=ostcb;
			ostcb.os_tcb_prev=pre_ostcb;
			ostcb.os_tcb_next=temp_ostcb;
		}
		else
		{
			pre_ostcb.os_tcb_next=ostcb;
			ostcb.os_tcb_prev=pre_ostcb;
		}
		System.out.println("创建任务成功");
		return true;
	}

	public static void traverse(OSTCB ostcb_list)
	{
		System.out.println("任务队列优先级如下:");
		ostcb_list=ostcb_list.os_tcb_next;
		while (ostcb_list!=null)
		{
			System.out.println("任务的优先级为:"+ostcb_list.os_tcb_prio);
			ostcb_list=ostcb_list.os_tcb_next;
		}
	}

	public static void main(String[] args) {
		OSTCB[] OSTCBPrioTbl = new OSTCB[64];
	}
}
