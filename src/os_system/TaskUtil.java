package os_system;

import javafx.concurrent.Task;

import javax.swing.tree.TreeNode;
import java.util.Scanner;

/**
 * @classname: TaskList
 * @description: TODO
 * Author : asus
 * Date : 2021/12/9 10:15
 * Version: 1.0
 **/
public class TaskUtil
{
	static int n=0,len=0;
	public static TaskNode[] createTaskList(int num)
	{
		//num表示创建链表长度
		TaskNode[] taskList = new TaskNode[num+1];
		//创建头指针
		TaskNode head = new TaskNode(-1);
		//令数组第0个位置充当哨兵节点
		taskList[0]=head;
		return taskList;
	}
	//插入节点
	public static boolean insertListNode(TaskNode[]taskNodes,int task_time)
	{
		int now_task_time = task_time;
		//如果链表满了，返回插入失败信息
		if(len==n)
		{
			System.out.println("链表已满，插入失败\n");
			return false;
		}
		//记录节点和节点的前驱
		TaskNode head = taskNodes[0];
		TaskNode temp_node = head.next;
		TaskNode temp_pre_node = head;
		int i=0;
		while (true)
		{
			i++;
			//如果插入节点的剩余时间大于当前节点，则继续遍历，并减去
			//当前节点的剩余时间
			if(temp_node!=null&&temp_node.task_time<=now_task_time) {
				now_task_time-=temp_node.task_time;
			}
			//找到插入位置
			else{
				len++;
				//将在插入节点后的数组元素右移一个单位
				for (int j=len;j>i;j--) taskNodes[j]=taskNodes[j-1];
				//创建插入节点
				TaskNode new_node = new TaskNode(now_task_time);
				//数组第i个元素指向插入节点
				taskNodes[i]=new_node;
				//如果插入的位置为空，则只需要更改前驱节点和插入节点指针
				if(temp_node==null)
				{
					new_node.pre=temp_pre_node;
					temp_pre_node.next=new_node;
				}
				//更改前驱节点、插入节点、后继节点位置
				else
				{
					//temp前插入节点
					TaskNode node_pre = temp_node.pre;
					new_node.pre=node_pre;
					new_node.next=temp_node;
					temp_node.pre=new_node;
					node_pre.next=new_node;
					//更新插入节点后的剩余时间
					while(temp_node!=null)
					{
						temp_node.task_time-=now_task_time;
						temp_node=temp_node.next;
					}
				}
				break;
			}
			temp_pre_node=temp_node;
			temp_node=temp_node.next;
		}
		System.out.println("插入成功!");
		return true;
	}
	public static boolean deleteListNode(TaskNode[]taskNodes,int index)
	{
		//删除位置不对
		if(index<=0||index>len)
		{
			System.out.println("删除的节点位置不存在");
			return false;
		}
		//找到删除节点、前驱节点和后继节点
		TaskNode delete_node = taskNodes[index];
		TaskNode pre_node = delete_node.pre;
		TaskNode next_node = delete_node.next;
		pre_node.next=next_node;
		//如果后继节点不为空，更新前驱节点和后继节点
		if(next_node!=null) {
			next_node.pre=pre_node;
			next_node.task_time+=delete_node.task_time;
		}
		//删除节点后的元素整体向前移动一个单位
		for(int i=index;i<len;i++) taskNodes[i]=taskNodes[i+1];
		//链表当前长度减一
		len--;
		System.out.println("删除成功!");
		return true;
	}
	public static void traverseTaskList(TaskNode[] taskNodes)
	{
		int sum=0;
		for (int i=1;i<=len;i++){
			System.out.println("第"+i+"个任务需要的时间为"+(taskNodes[i].task_time+sum));
			sum+=taskNodes[i].task_time;
		}

	}
	public static void main(String[] args) {
		System.out.println("请输入要创建的任务链表的长度");
		Scanner sc = new Scanner(System.in);
		n = sc.nextInt();
		int[] task_time_list = new int[n];
		System.out.print("请分别输入任务执行时间");
		for(int i=0;i<n;i++)
		{
			task_time_list[i]=sc.nextInt();
		}
		TaskNode[] taskNodes = createTaskList(n);
		System.out.println("创建链表成功");
		for(int i=0;i<n;i++)
		{
			insertListNode(taskNodes,task_time_list[i]);
		}
		traverseTaskList(taskNodes);
		System.out.println("请输入要删除的节点");
		int index = sc.nextInt();
		deleteListNode(taskNodes,index);
		/*for(int i=0;i<5;i++)
		{
			insertListNode(taskNodes,task_time_list[i]);
		}*/
		traverseTaskList(taskNodes);

	}
}

class TaskNode {
	Integer task_time;
	TaskNode pre,next;
	public TaskNode(int task_time)
	{
		this.task_time=task_time;
		this.pre=this.next=null;
	}
}
