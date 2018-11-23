import React from 'react';
import styled from 'styled-components';
import Chart from '../chart';

const setWeekValue = (week) => {
  if (week === 0) return 'Current week';
  if (week === 1) return `${week} week ago`;
  return `${week} weeks ago`;
}

const ChartStatistics = ({ratingStatistic, gamesCountStatistic}) => {

  const mappedRatingStatistic = ratingStatistic.map((item, index) =>
    ({rating: item, week: setWeekValue(9 - index)}));
  const mappedGamesCountStatistic = gamesCountStatistic.map((item, index) =>
    ({games: item, week: setWeekValue(9 - index)}));

  return (
    <Content>
      <Chart data={mappedRatingStatistic} lineDataKey='rating' xDataKey='week' title='Rating per week'/>
      <Chart data={mappedGamesCountStatistic} lineDataKey='games' xDataKey='week' title='Count of games per week'/>
    </Content>
  )
}

export default ChartStatistics;

const Content = styled.div`
  display: flex;
  flex-direction: column;
`;