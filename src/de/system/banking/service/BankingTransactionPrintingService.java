package de.system.banking.service;

import de.system.banking.model.BankingTransaction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;

public class BankingTransactionPrintingService {

    private String rootLocation = "";

    private BankingTransactionPrintingService() {
    }

    public BankingTransactionPrintingService(String rootLocation) {
        this.rootLocation = rootLocation;
        try {
            Files.createDirectories(Path.of(rootLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile(Collection<BankingTransaction> bankingTransactions, String fileName) {
        try {
            FileWriter writer = new FileWriter(rootLocation + "/" + fileName + ".csv");

            writer.write("Date, Type, Amount, Failed\n");

            StringBuilder stringBuilder = new StringBuilder();

            for (BankingTransaction bankingTransaction : bankingTransactions) {
                stringBuilder.append(Instant.ofEpochMilli(bankingTransaction.getTime()))
                        .append(", ").append(bankingTransaction.getTransactionType()).append(", ").append(bankingTransaction.getAmount()).append(", ").append(bankingTransaction.isFailed()).append("\n");
            }

            writer.write(stringBuilder.toString());

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
