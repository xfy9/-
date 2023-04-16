package os_system;

import java.util.Scanner;

/**
 * @classname: OSFlagBlockUtil
 * @description: TODO
 * Author : asus
 * Date : 2021/12/29 9:56
 * Version: 1.0
 **/
public class OSFlagBlockUtil {
	static BitMap ready_bitmap = new BitMap("ready_bitmap");//优先级位图
	static OSTCB[] OSTCBPrioTbl = new OSTCB[64];//优先级数组
	final static int OS_EVENT_TYPE_FLAG=1; //事件标志
	static OSTCB curr_task=null;//当前任务
	public static void OS_FlagBlock(OSFlagGrp pgrp, //事件标志组
									OSFlagNode pnode,//任务等待信息节点
									OSFlags flags,//任务等待的标志
									int wait_type,//等待类型，用来唤醒任务
									int timeout) //等待时间
	{
		OSFlagNode pnode_next=null;
		//修改当前任务TCB的参数为挂起状态
		curr_task.os_tcb_stat=OSTASKSUSPEND.OS_STAT_FLAG;
		curr_task.os_tcb_statpend=OSTASKSUSPEND.OS_STAT_PEND_OK;
		curr_task.os_tcb_dly=timeout;
		//当前任务等待节点指向任务等待信息节点
		curr_task.OSTCBFlagNode=pnode;

		//修改pnode的状态
		pnode.OSFlagNodeFlags=flags;
		pnode.OSFlagNodeWaitType=wait_type;
		pnode.OSFlagNodeTCB=curr_task;
		//采用头插法，将节点插入到事件标志等待列表
		pnode.OSFlagNodeNext=pgrp.OSFlagWaitList.OSFlagNodeNext;
		pnode.OSFlagNodePrev=pgrp.OSFlagWaitList;
		pnode.OSFlagNodeFlagGrp=pgrp;
		//将当前pgrp等待列表的头节点向后移一位，注意这个为双向链表
		pnode_next = pgrp.OSFlagWaitList;
		if(pnode_next!=null)
		{
			pnode_next.OSFlagNodeNext=pnode;
		}
		//pgrp等待列表指针，指向新的头节点
		pgrp.OSFlagWaitList=pnode;
		//在就绪列表和就绪组中，取消就绪标志，挂起任务
		BitMapUtil.suspendTask(ready_bitmap,curr_task.os_tcb_prio);
	}

	public static boolean create_wait_node(OSFlagNode flagNodeList,OSFlagNode node)
	{
		node.OSFlagNodeNext=flagNodeList.OSFlagNodeNext;
		node.OSFlagNodePrev=flagNodeList;
		flagNodeList.OSFlagNodeNext=node;
		return true;
	}
	public static void traverse(OSFlagNode flagNodeList)
	{
		OSFlagNode temp = flagNodeList.OSFlagNodeNext;
		while(temp!=null)
		{
			System.out.print(temp.prior+" ");
			temp=temp.OSFlagNodeNext;
		}
		System.out.println();
	}

	public static void main(String[] args) {

		OSFlagNode head_node = new OSFlagNode(null,0,null,null,null,null,-1);
		System.out.println("阻塞前的等待队列优先级为：");
		for(int i=5;i>=2;i--)
		{
			OSFlagNode tnode = new OSFlagNode(null,0,null,null,null,null,i);
			create_wait_node(head_node,tnode);
		}
		traverse(head_node);
		OSTCB head=new OSTCB(-1,"head");
		Scanner sc = new Scanner(System.in);
		System.out.println("请输入您创建任务的优先级");
		int pri  = sc.nextInt();
		OSTCBUtil.create_task(pri,OSTCBPrioTbl,head,ready_bitmap);
		OSFlagNode tnode = new OSFlagNode(null,0,null,null,null,null,1);
		curr_task=head.os_tcb_next;
		System.out.println("任务阻塞前就绪位图状态");
		BitMapUtil.get_task_map_status(ready_bitmap);
		System.out.println("请输入阻塞事件个数");
		OSFlags osFlags = new OSFlags(sc.nextInt());
		OSFlagGrp osFlagGrp = new OSFlagGrp(OS_EVENT_TYPE_FLAG,head_node,1,"wait");
		System.out.println("进入阻塞任务方法");
		OS_FlagBlock(osFlagGrp,tnode,osFlags,1,100);
		System.out.println("阻塞后的等待队列优先级为：");
		traverse(head_node);
		System.out.println("任务阻塞后位图状态");
		BitMapUtil.get_task_map_status(ready_bitmap);


	}
}
class OSFlagBlock
{

}
class OSFlagGrp //事件标志组
{
	int  OSFlagType; //信号量集标志,必须为OS_EVENT_TYPE_FLAG
	OSFlagNode OSFlagWaitList; //指向任务等待组第一个节点的指针
	int OSFlagFlags; //信号量集的长度
	String OSFlagName;//事件标志组名字

	public OSFlagGrp(int OSFlagType,OSFlagNode OSFlagWaitList,int OSFlagFlags,String OSFlagName)
	{
		this.OSFlagType=OSFlagType;
		this.OSFlagWaitList=OSFlagWaitList;
		this.OSFlagFlags=OSFlagFlags;
		this.OSFlagName=OSFlagName;
	}

}
class OSFlags
{
	int[] flags;
	public OSFlags(int n)
	{
		this.flags=new int[n];
	}
}
class OSFlagNode
{
	OSFlags OSFlagNodeFlags;
	int prior;
	int OSFlagNodeWaitType;//事件等待类型
	OSTCB OSFlagNodeTCB;//等待TCB
	OSFlagNode OSFlagNodeNext;//后继节点
	OSFlagNode OSFlagNodePrev;//前驱节点
	OSFlagGrp OSFlagNodeFlagGrp;//事件标志组

	public OSFlagNode(OSFlags OSFlagNodeFlags,int OSFlagNodeWaitType,OSTCB OSFlagNodeTCB,
					  OSFlagNode OSFlagNodeNext,OSFlagNode OSFlagNodePrev,OSFlagGrp OSFlagNodeFlagGrp,
					  int prior)
	{
		this.OSFlagNodeFlags=OSFlagNodeFlags;
		this.OSFlagNodeWaitType=OSFlagNodeWaitType;
		this.OSFlagNodeTCB=OSFlagNodeTCB;
		this.OSFlagNodeNext=OSFlagNodeNext;
		this.OSFlagNodePrev=OSFlagNodePrev;
		this.OSFlagNodeFlagGrp=OSFlagNodeFlagGrp;
		this.prior=prior;
	}


}