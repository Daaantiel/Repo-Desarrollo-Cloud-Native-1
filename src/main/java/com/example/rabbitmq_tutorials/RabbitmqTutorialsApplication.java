package com.example.rabbitmq_tutorials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Scanner;

@SpringBootApplication
public class RabbitmqTutorialsApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(RabbitmqTutorialsApplication.class, args);
        Sender sender = ctx.getBean(Sender.class);

        System.out.println("\n" + "=".repeat(60));
        System.out.println(" RabbitMQ Hello World con Spring Boot");
        System.out.println("=".repeat(60));
        System.out.println(" [OK] Aplicación iniciada correctamente");
        System.out.println(" [OK] RabbitMQ conectado en localhost:5672");
        System.out.println(" [OK] Cola 'hello' lista para usar\n");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- Menú ---");
            System.out.println("1. Enviar mensaje");
            System.out.println("2. Salir");
            System.out.print("Selecciona opción (1-2): ");

            String option = scanner.nextLine().trim();

            if ("1".equals(option)) {
                System.out.print("Escribe el mensaje a enviar: ");
                String msg = scanner.nextLine();
                sender.sendMessage(msg);
            } else if ("2".equals(option)) {
                running = false;
                System.out.println("¡Hasta luego!");
            } else {
                System.out.println("Opción no válida.");
            }
        }
        scanner.close();
        System.exit(0);
    }
}