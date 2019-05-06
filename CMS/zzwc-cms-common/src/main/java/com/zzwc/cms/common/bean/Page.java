package com.zzwc.cms.common.bean;

import java.util.List;

public class Page<T> {
	//总记录数
	private long totalCount;
	//每页记录数
	private int pageSize;
	//总页数
	private long totalPage;
	//当前页数
	private int  pageNum;
	//列表数据
	private List<?> list;

	public Page(long totalCount, List<T> list, int pageNum,long totalPage,int pageSize) {
		super();
		this.totalCount = totalCount;
		this.list = list;
		this.pageNum=pageNum;
		this.totalPage=totalPage;
		this.pageSize=pageSize;
	}
	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}
}
