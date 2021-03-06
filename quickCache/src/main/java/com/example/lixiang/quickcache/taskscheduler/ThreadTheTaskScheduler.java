package com.example.lixiang.quickcache.taskscheduler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Created by lixiang on 2017/3/26.
 */
 public abstract class ThreadTheTaskScheduler<T> {
    public ThreadTheTaskScheduler(){init();}

    private static final int DEAFULT_THREAD_COUNT = 1;

    /**
     * 任务队列：是一个list链表集合
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    /**
     * 初始化
     *
     * @param threadCount
     * @param type
     */



    final Semaphore semp = new Semaphore(10);
    private void init( )
    {
        mTaskQueue = new LinkedList<Runnable>();

        //初始化后台轮询线程
        initBackThread();
//        当还需要有其他初始化操作就交给子类去实现
        CacheContainer();
    }



    /**
     * 初始化后台轮询线程
     */
    private void initBackThread()
    {
        if(mPoolThread == null)
        {
            // 后台轮询的子线程
            mPoolThread = new Thread()
            {
                @Override
                public void run()
                {
//					开启一个looper循环【在这个过程中looper在这个子线程中一直处于循环状态，但一直循环最多同时只能执行threadCount；因为有
//					线程池和Semaphore信号量的阻塞】
                    Looper.prepare();
                    mPoolThreadHandler = new Handler()
                    {
                        @Override
                        public void handleMessage(Message msg)
                        {
                            if(msg.what == 1)
                            {
                                // 线程池去取出一个任务进行执行；获取到消息就获取许可，执行下面的代码
                                Runnable task = getTask();
                                try {
                                    semp.acquire();
//                                    task.run();
                                    new Thread(task).start();
                                } catch (InterruptedException e) {

                                }
                                finally {
                                    semp.release();
                                }
                            }
                        }
                    };
                    Looper.loop();
                };
            };
        }


        mPoolThread.start();
    }



    /**
     * 从任务队列取出一个方法
     * @return
     */
    private Runnable getTask()
    {
        try {

            if(mTaskQueue.size()>0)
            {
                return mTaskQueue.removeFirst();
            }
        }catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }



    /**
     * 根据传入的参数，新建一个任务
     * 单独开启的子线程；
     * 每一次加载都会开启一个；然后存储在list集合中
     * @return
     */
    public Runnable buildTask(T taskCaheBean)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                runThreadPoolChildThread(taskCaheBean);
            }
        };
    }

    /**
     * @author lixiang 往队列里添加一个元素
     *
     */
    public synchronized void addTask(Runnable runnable)
    {
        mTaskQueue.add(runnable);
        mPoolThreadHandler.sendEmptyMessage(1);
    }

    /**
     * @author lixiang 队列线程池中需要执行的方法
     **/
    public abstract void runThreadPoolChildThread(T taskCaheBean);

    /**
     * @author lixiang 初始化缓存容器
     **/
    public abstract void CacheContainer();

}
