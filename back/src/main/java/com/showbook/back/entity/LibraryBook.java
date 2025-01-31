package com.showbook.back.entity;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.minidev.json.annotate.JsonIgnore;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class LibraryBook {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long libraryBookId;

	@Column(nullable = false)
	private int readStatus; // 0 = wish, 1 = reading, 2= finish

	@Column
	private LocalDate finishedDate; // readStatus가 2인 경우에만

	@ManyToOne
	@JoinColumn(name = "library_id",nullable = false)
	private Library library;

	@ManyToOne
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Builder
	public LibraryBook(int readStatus, LocalDate finishedDate, Library library, Book book) {
		this.readStatus = readStatus;
		this.finishedDate = finishedDate;
		this.library = library;
		this.book = book;
	 }

	 public void setReadStatus(int newReadStatus){
		this.readStatus = newReadStatus;
	 }
	public void setFinishedDate(LocalDate date){
		this.finishedDate = date;
	}
}
