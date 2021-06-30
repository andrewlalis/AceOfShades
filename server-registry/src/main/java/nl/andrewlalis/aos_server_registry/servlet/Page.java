package nl.andrewlalis.aos_server_registry.servlet;

import java.util.List;

public class Page<T> {
	private final List<T> contents;
	private final int elementCount;
	private final int pageSize;
	private final int currentPage;
	private final boolean firstPage;
	private final boolean lastPage;

	public Page(List<T> contents, int currentPage, int pageSize) {
		this.contents = contents;
		this.elementCount = contents.size();
		this.pageSize = pageSize;
		this.currentPage = currentPage;
		this.firstPage = currentPage == 0;
		this.lastPage = this.elementCount < this.pageSize;
	}

	public List<T> getContents() {
		return contents;
	}

	public int getElementCount() {
		return elementCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public boolean isFirstPage() {
		return firstPage;
	}

	public boolean isLastPage() {
		return lastPage;
	}
}
