package com.showbook.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.showbook.back.common.constants.ErrorCode;
import com.showbook.back.common.exception.CustomException;
import com.showbook.back.dto.response.BookDetailResponseDTO;
import com.showbook.back.dto.response.BookPurchaseResponseDTO;
import com.showbook.back.entity.Book;
import com.showbook.back.entity.Bookmark;
import com.showbook.back.entity.Member;
import com.showbook.back.repository.BookRepository;
import com.showbook.back.repository.BookmarkRepository;
import com.showbook.back.security.model.PrincipalDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class BookService {

	@Value("${purchase.prefix}")
	private String prefix;
	@Value("${purchase.suffix}")
	private String suffix;
	@Value("${purchase.bridge}")
	private String bridge;

	private final BookmarkRepository bookmarkRepository;
	private final BookRepository bookRepository;

	public BookDetailResponseDTO getDetail(Long bookId, PrincipalDetails principalDetails) {
		Book book = bookRepository.findById(bookId).orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
		Member member = principalDetails.getMember();
		Boolean isLiked = bookmarkRepository.findByBookAndMember(book, member).isPresent();

		//feature에 결측치를 가지는 book은 db에 없음
		return BookDetailResponseDTO.builder()
			.book(book)
			.isLiked(isLiked)
			.build();
	}

	public BookPurchaseResponseDTO getPurchaseUrl(Long bookId) {

		Book book = bookRepository.findById(bookId).
			orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

		return BookPurchaseResponseDTO.builder()
			.url(transformTitleToUrl(book))
			.build();
	}

	public String transformTitleToUrl(Book book) {
		StringBuilder result = new StringBuilder();
		String target1 = book.getTitle();
		String target2 = book.getPublisher();

		result.append(prefix);

		for (int i = 0; i < target1.length(); i++) {
			char tmp = target1.charAt(i);
			if (tmp == ' ') result.append(bridge);
			else result.append(tmp);
		}

		result.append(bridge).append(target2).append(suffix);
		return result.toString();
	}

	public void doBookmark(PrincipalDetails principalDetails, Long bookId) {
		Member member = principalDetails.getMember();
		Book book = bookRepository.findById(bookId).orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

		if(bookmarkRepository.findByBookAndMember(book, member).isEmpty()) {
			Bookmark bookmark = Bookmark.builder()
				.member(member)
				.book(book)
				.build();
			bookmarkRepository.save(bookmark);
		}
	}
	public void deleteBookmark(PrincipalDetails principalDetails, Long bookId) {
		Member member = principalDetails.getMember();
		Book book = bookRepository.findById(bookId).orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

		if(bookmarkRepository.findByBookAndMember(book, member).isPresent()) {
			Bookmark bookmark = bookmarkRepository.findByBookAndMember(book, member).orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));
			bookmarkRepository.delete(bookmark);
		}
	}
}