package com.nter.projectg.games.common.cards;


public class Card {

    public enum Suit {
        SPADES,
        HEARTS,
        DIAMONDS,
        CLUBS;
    }

    public enum Rank {
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        ACE;
    }

    private Suit mSuit;
    private Rank mRank;

    public Card(Suit suit, Rank rank) {
        this.mSuit = suit;
        this.mRank = rank;
    }

    public Suit getSuit() {
        return mSuit;
    }

    public Rank getRank() {
        return mRank;
    }

    public int getValue() {
        return mRank.ordinal() + 2;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && o instanceof Card && ((Card) o).mRank == mRank && ((Card) o).mSuit == mSuit);
    }

    @Override
    public String toString() {
        return getRank().toString() + " " + getSuit().toString();
    }

}
