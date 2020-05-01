package com.nter.projectg.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "LOBBY")
public class LobbyModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LOBBY_ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "LOBBY_GAME", joinColumns = @JoinColumn(name = "LOBBY_ID"), inverseJoinColumns = @JoinColumn(name = "GAME_ID"))
    private Set<LobbyModel> lobbyGame;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "LOBBY_USER", joinColumns = @JoinColumn(name = "LOBBY_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private Set<LobbyModel> lobbyUser;

}
