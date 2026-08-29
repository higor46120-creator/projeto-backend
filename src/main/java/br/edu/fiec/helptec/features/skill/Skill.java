package br.edu.fiec.helptec.features.skill;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Catálogo de skills existentes (ex: REDE, HARDWARE, SOFTWARE, IMPRESSORA...)
@Entity
@Table(name = "tb_skill")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSkill;

    @Column(nullable = false, unique = true)
    private String nome;
}
