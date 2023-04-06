package com.nter.projectg.games.blackjack;

import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.common.cards.Card;
import com.nter.projectg.lobby.Lobby;
import com.nter.projectg.model.blackjack.BlackJackMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackJackGame extends Game<BlackJackMessage, BlackJackPlayer> {

    private static final Logger logger = LoggerFactory.getLogger(BlackJackGame.class);

    private PlayerHandler playerHandler;

    private Assets assets;

    public BlackJackGame(Lobby lobby) {
        super(lobby, "BlackJack", 1, 10);

        logger.debug("Initializing Black Jack: {}", this);
        initializeAssets();
        logger.info("Initialized Black Jack: {}", this);
    }

    private void initializeAssets() {
        logger.debug("Initializing players and assets:");

        playerHandler = new PlayerHandler(getPlayers());
        assets = new Assets(playerHandler.getPlayers());

        logger.info("Initialized players and assets: {}", playerHandler);
    }
    @Override
    protected BlackJackPlayer createPlayer(String name) {
        return new BlackJackPlayer(name, sendToPlayer(name));
    }

    @Override
    protected void handleGame(BlackJackMessage message) {

    }

    @Override
    public void start() {
        super.start();

        logger.info("Dealing first hand for {} players", playerHandler.getPlayerCount());
        for (BlackJackPlayer player : playerHandler.getPlayers()) {

            Card randomCard1 = assets.getCardDeck().pullRandom();
            Card randomCard2 = assets.getCardDeck().pullRandom();
            logger.info("{} was dealt {} of {}", player.getName(), randomCard1.getRank(), randomCard1.getSuit());
            logger.info("{} was dealt {} of {}", player.getName(), randomCard2.getRank(), randomCard2.getSuit());

            BlackJackMessage dealermessage = buildGameMessage(BlackJackMessage.GameMessageType.DEAL, randomCard1.toString() + "," + randomCard2.toString());
            sendToPlayer(player.getName(), dealermessage);
        }
    }

    private BlackJackMessage buildGameMessage(BlackJackMessage.GameMessageType type, String content) {
        return buildGameMessage(type, getName(), content);
    }

    private BlackJackMessage buildGameMessage(BlackJackMessage.GameMessageType type, String sender, String content) {
        BlackJackMessage message = new BlackJackMessage();
        message.setSender(sender);
        message.setGameType(type);
        message.setContent(content);
        return message;
    }
}
