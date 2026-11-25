
package com.example.rabbitmq_tutorials;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RabbitmqTutorialsApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                SpringApplication.run(
                    RabbitmqTutorialsApplication.class, args
                );

        Sender sender = context.getBean(Sender.class);
        Scanner scanner = new Scanner(System.in);

        int opcion = 0;

        while (opcion != 7) {

            // ==========================================
            // MENU POKEMARKET360 - RABBITMQ
            // ==========================================

            System.out.println(
                "\n===== POKEMARKET360 - RABBITMQ ====="
            );

            System.out.println("1. EMAIL - Direct");
            System.out.println("2. STOCK - Direct");
            System.out.println("3. FACTURA - Direct");
            System.out.println("4. EMAIL - Topic");
            System.out.println("5. STOCK - Topic");
            System.out.println("6. FACTURA - Topic");
            System.out.println("7. Salir");

            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(
                    scanner.nextLine()
                );

            } catch (NumberFormatException e) {

                System.out.println("Opcion invalida.");
                continue;
            }

            if (opcion >= 1 && opcion <= 6) {

                System.out.print("Ingrese mensaje: ");
                String mensaje = scanner.nextLine();

                switch (opcion) {

                    // ==================================
                    // EXCHANGE DIRECT
                    // ==================================

                    case 1:
                        sender.sendMessage(
                            RabbitMQConfig.EXCHANGE_DIRECT,
                            RabbitMQConfig.KEY_EMAIL,
                            mensaje
                        );
                        break;

                    case 2:
                        sender.sendMessage(
                            RabbitMQConfig.EXCHANGE_DIRECT,
                            RabbitMQConfig.KEY_STOCK,
                            mensaje
                        );
                        break;

                    case 3:
                        sender.sendMessage(
                            RabbitMQConfig.EXCHANGE_DIRECT,
                            RabbitMQConfig.KEY_INVOICE,
                            mensaje
                        );
                        break;

                    // ==================================
                    // EXCHANGE TOPIC
                    // ==================================

                    case 4:
                        sender.sendMessage(
                            RabbitMQConfig.EXCHANGE_TOPIC,
                            "email.high",
                            mensaje
                        );
                        break;

                    case 5:
                        sender.sendMessage(
                            RabbitMQConfig.EXCHANGE_TOPIC,
                            RabbitMQConfig.KEY_STOCK,
                            mensaje
                        );
                        break;

                    case 6:
                        sender.sendMessage(
                            RabbitMQConfig.EXCHANGE_TOPIC,
                            "invoice.pdf",
                            mensaje
                        );
                        break;

                    default:
                        break;
                }

            } else if (opcion != 7) {

                System.out.println("Opcion invalida.");
            }
        }

        scanner.close();
        context.close();
    }
}
