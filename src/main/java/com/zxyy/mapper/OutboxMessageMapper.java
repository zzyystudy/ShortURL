package com.zxyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxyy.entity.OutboxMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OutboxMessageMapper extends BaseMapper<OutboxMessage> {
    @Select("""
            SELECT * FROM outbox_message
            WHERE status = 0 AND next_retry_time <= CURRENT_TIMESTAMP
            ORDER BY next_retry_time, id LIMIT #{limit}
            """)
    List<OutboxMessage> findReady(@Param("limit") int limit);

    @Update("""
            UPDATE outbox_message SET status = 1, sent_time = CURRENT_TIMESTAMP, last_error = NULL
            WHERE id = #{id} AND status = 0
            """)
    int markSent(@Param("id") Long id);

    @Update("""
            UPDATE outbox_message SET retry_count = retry_count + 1,
              next_retry_time = TIMESTAMPADD(SECOND, #{delaySeconds}, CURRENT_TIMESTAMP), last_error = #{error}
            WHERE id = #{id} AND status = 0
            """)
    int postpone(@Param("id") Long id, @Param("delaySeconds") long delaySeconds,
                 @Param("error") String error);
}
