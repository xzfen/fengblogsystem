package net.feng.blog.pojo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Table ( name ="tb_article" )
public class Article {

  	@Id
	private String id;

  	@Column(name = "title" )
	private String title;

  	@Column(name = "user_id" )
	private String userId;

  	@Column(name = "category_id" )
	private String categoryId;

  	@Column(name = "content" )
	private String content;
    //类型（0表示富文本，1表示markdown）
  	@Column(name = "type" )
	private String type;
    @Column(name = "cover" )
    private String cover;
	//0表示删除、1表示已经发布、2表示草稿、3表示置顶
  	@Column(name = "state" )
	private String state = "1";

  	@Column(name = "summary" )
	private String summary;

  	@Column(name = "label" )
	private String label;

  	@Column(name = "view_count" )
	private long viewCount;

  	@Column(name = "create_time" )
	private Date createTime;

  	@Column(name = "update_time" )
	private Date updateTime;

  	@OneToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

  	@Transient
  	private List<String> labels = new ArrayList<>();



    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getLabel() {
        //打散到集合
        this.labels.clear();
        if(this.label != null) {
            if (!this.label.contains("-")) {
                this.labels.add(this.label);
            } else {
                String[] split = this.label.split("-");
                List<String> strings = Arrays.asList(split);
                this.labels.addAll(strings);
            }
        }
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public long getViewCount() {
		return viewCount;
	}

	public void setViewCount(long viewCount) {
		this.viewCount = viewCount;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
