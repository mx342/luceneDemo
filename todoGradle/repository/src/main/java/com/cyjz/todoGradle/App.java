package com.cyjz.todoGradle;

import com.cyjz.pojo.TodoBean20;
import com.cyjz.solr.TodoBean2;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        int i = 0;
        Scanner scanner = new Scanner(System.in);
        while ( ++ i > 0){
            System.err.println( i + "请输入todo item的name");
            TodoItem todoItem = new TodoItem(scanner.nextLine());
            System.err.println(todoItem.toString());
            TodoBean20 to = new TodoBean20();
            TodoBean2 to2 = new TodoBean2();
        }
    }
}
