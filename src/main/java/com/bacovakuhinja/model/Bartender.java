package com.bacovakuhinja.model;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "bartenders")
@PrimaryKeyJoinColumn(name = "b_id")
public class Bartender extends Employee {
}
