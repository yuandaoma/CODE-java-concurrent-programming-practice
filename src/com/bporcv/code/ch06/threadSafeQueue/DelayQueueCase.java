package com.bporcv.code.ch06.threadSafeQueue;

import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DelayQueueCase
 * @Description 线程安全的延迟队列
 * @Author Administrator
 * @Date 2020/3/22 9:08
 * @Version 1.0
 */
public class DelayQueueCase {

    public static void main(String[] args) {
        DelayQueue<Event> queue = new DelayQueue<>();
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            Task task = new Task(i + 1, queue);
            threads[i] = new Thread(task);
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        do {
            int counter = 0;
            Event event;
            do {
                event = queue.poll();
                if (event != null) {
                    counter++;
                }

            } while (event != null);
            System.out.printf("At %s you have read %d events\n", new Date(), counter);
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (queue.size()>0);

    }


    public static class Event implements Delayed {
        private Date startDate;

        public Event(Date startDate) {
            this.startDate = startDate;
        }

        @Override
        public int compareTo(Delayed o) {
            long result = this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public long getDelay(TimeUnit unit) {
            Date now = new Date();
            long diff = startDate.getTime() - now.getTime();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }
    }


    public static class Task implements Runnable {
        private int id;
        private DelayQueue<Event> queue;

        public Task(int id, DelayQueue<Event> queue) {
            this.id = id;
            this.queue = queue;
        }

        @Override
        public void run() {
            Date now = new Date();
            Date delay = new Date();
            delay.setTime(now.getTime() + (id * 1000));
            System.out.printf("Thread %s: %s\n", id, delay);
            for (int i = 0; i < 100; i++) {
                Event event = new Event(delay);
                queue.add(event);
            }
        }
    }
}
