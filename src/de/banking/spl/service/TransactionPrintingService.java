package de.banking.spl.service;

import de.banking.spl.model.Transaction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;

public class TransactionPrintingService {

    private String rootLocation = "";

    private TransactionPrintingService() {
    }

    public TransactionPrintingService(String rootLocation) {
        this.rootLocation = rootLocation;
        try {
            Files.createDirectories(Path.of(rootLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile(Collection<Transaction> transactions, String fileName) {
        try {
            FileWriter writer = new FileWriter(rootLocation + "/" + fileName + ".csv");

            writer.write("Date, Type, Amount, Failed\n");

            StringBuilder stringBuilder = new StringBuilder();

            for (Transaction transaction : transactions) {
                stringBuilder.append(Instant.ofEpochMilli(transaction.getTime()))
                        .append(", ").append(transaction.getTransactionType()).append(", ").append(transaction.getAmount()).append(", ").append(transaction.isFailed()).append("\n");
            }

            writer.write(stringBuilder.toString());

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
