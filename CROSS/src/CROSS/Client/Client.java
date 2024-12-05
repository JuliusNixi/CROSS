package CROSS.Client;

import java.util.Scanner;

import CROSS.Enums.ClientActions;

public class Client {
    
    public static void CLI(){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Client CLI ->");

            ClientActions action;

            String command = scanner.nextLine().toLowerCase().trim();
            if (command.startsWith("register")){
                action = ClientActions.REGISTER;
            } else if (command.startsWith("login")){
                action = ClientActions.LOGIN;
            } else if (command.startsWith("updateCredentials")){
                action = ClientActions.UPDATE_CREDENTIALS;
            } else if (command.startsWith("logout")){
                action = ClientActions.LOGOUT;
            } else if (command.startsWith("insertMarketOrder")){
                action = ClientActions.INSERT_MARKET_ORDER;
            } else if (command.startsWith("insertLimitOrder")){
                action = ClientActions.INSERT_LIMIT_ORDER;
            } else if (command.startsWith("insertStopOrder")){
                action = ClientActions.INSERT_STOP_ORDER;
            } else if (command.startsWith("cancelOrder")){
                action = ClientActions.CANCEL_ORDER;
            } else if (command.startsWith("getPriceHistory")){
                action = ClientActions.GET_PRICE_HISTORY;
            }  else {
                // TODO: ERROR.
            }

        }
    }

}
