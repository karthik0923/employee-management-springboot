package com.example.employee.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Stable JSON wrapper around a Spring Data {@code Page}.
 *
 * Why this class exists:
 * Returning {@code Page<EmployeeResponse>} works, but it exposes Spring Data
 * internals (pageable, sort, empty, etc.) to API clients. This DTO keeps only
 * the fields a frontend actually needs.
 *
 * Interview concept:
 * {@code Page} is 0-based. {@code page=0} is the first page. {@code size} is
 * how many rows per page. {@code sort=lastName,asc} becomes an {@code Order}.
 */
public class PagedResponse<T> {

	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;

	public PagedResponse() {
	}

	public PagedResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean first,
			boolean last) {
		this.content = content;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.first = first;
		this.last = last;
	}

	public static <T> PagedResponse<T> from(Page<T> page) {
		return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages(), page.isFirst(), page.isLast());
	}

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}
}
