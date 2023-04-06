package com.nter.projectg.games.common.cards;

import java.util.ArrayList;
import java.util.Random;

public class CardDeck {
    private ArrayList<Card> mCards;
    private ArrayList<Card> mPulledCards;
    private Random mRandom;



    public CardDeck() {
        mRandom = new Random();
        mPulledCards = new ArrayList<Card>();
        mCards = new ArrayList<Card>(Card.Suit.values().length * Card.Rank.values().length);
        reset();
    }

    public void reset() {
        mPulledCards.clear();
        mCards.clear();
        /* Creating all possible cards... */
        for (Card.Suit s : Card.Suit.values()) {
            for (Card.Rank r : Card.Rank.values()) {
                Card c = new Card(s, r);
                mCards.add(c);
            }
        }
    }

    /**
     * get a random card, removing it from the pack
     * @return
     */
    public Card pullRandom() {
        if (mCards.isEmpty())
            return null;

        Card res = mCards.remove(randInt(0, mCards.size() - 1));
        if (res != null)
            mPulledCards.add(res);
        return res;
    }

    /**
     * Get a random cards, leaves it inside the pack
     * @return
     */
    public Card getRandom() {
        if (mCards.isEmpty())
            return null;

        Card res = mCards.get(randInt(0, mCards.size() - 1));
        return res;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public int randInt(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = mRandom.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
