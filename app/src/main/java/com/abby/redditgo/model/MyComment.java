package com.abby.redditgo.model;

import com.oissela.software.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import net.dean.jraw.models.Comment;

import java.util.ArrayList;
import java.util.List;

public class MyComment implements MultiLevelExpIndListAdapter.ExpIndData {
    private int mIndentation;
    private List<MyComment> mChildren;
    private boolean mIsGroup;
    private int mGroupSize;
    private final Comment comment;

    public MyComment(Comment comment, int indentation) {
        this.comment = comment;
        this.mIndentation = indentation;
        mChildren = new ArrayList<MyComment>();
    }

    @Override
    public List<? extends MultiLevelExpIndListAdapter.ExpIndData> getChildren() {
        return mChildren;
    }

    @Override
    public boolean isGroup() {
        return mIsGroup;
    }

    @Override
    public void setIsGroup(boolean value) {
        mIsGroup = value;
    }

    @Override
    public void setGroupSize(int groupSize) {
        mGroupSize = groupSize;
    }

    public Comment getComment() { return comment; }
    public int getGroupSize() {
        return mGroupSize;
    }

    public int getIndentation() {
        return mIndentation;
    }

    private void setIndentation(int indentation) {
        mIndentation = indentation;
    }
}