# 데이터 모델

## users

```json
{
  "id": "firebase_uid",
  "displayName": "홍길동",
  "email": "user@example.com",
  "photoUrl": "https://...",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## trips

```json
{
  "id": "trip_id",
  "title": "오사카 3박 4일 여행",
  "destination": "오사카",
  "startDate": "2026-06-10",
  "endDate": "2026-06-13",
  "memo": "맛집과 쇼핑 중심",
  "ownerId": "firebase_uid",
  "inviteCode": "AB12CD",
  "memberIds": ["firebase_uid"],
  "isDomestic": false,
  "baseCurrency": "JPY",
  "exchangeRateToKrw": 9.3,
  "mapProvider": "naver",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## trips/{tripId}/members

```json
{
  "userId": "firebase_uid",
  "role": "owner",
  "displayName": "홍길동",
  "email": "user@example.com",
  "photoUrl": "https://...",
  "joinedAt": "timestamp"
}
```

`role` 값:

- `owner`
- `member`

## trips/{tripId}/scheduleItems

```json
{
  "id": "schedule_item_id",
  "tripId": "trip_id",
  "date": "2026-06-10",
  "dayNumber": 1,
  "time": "09:00",
  "title": "간사이공항 도착",
  "placeName": "간사이국제공항",
  "addressOrQuery": "Kansai International Airport",
  "memo": "입국 후 라피트 탑승",
  "estimatedCost": 1500,
  "currency": "JPY",
  "estimatedCostKrw": 13950,
  "category": "transport",
  "durationMemo": "공항에서 난바까지 약 40분",
  "order": 1000,
  "createdBy": "firebase_uid",
  "updatedBy": "firebase_uid",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

`category` 값:

- `tour`
- `food`
- `transport`
- `hotel`
- `shopping`
- `etc`

## trips/{tripId}/packingItems

```json
{
  "id": "packing_item_id",
  "tripId": "trip_id",
  "name": "여권",
  "isChecked": false,
  "order": 1000,
  "createdBy": "firebase_uid",
  "updatedBy": "firebase_uid",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## 날짜 계산

Day 문서는 별도로 저장하지 않는다.

앱은 `startDate`와 `endDate`를 기준으로 Day 목록을 계산한다.

예:

- 시작일: 2026-06-10
- 종료일: 2026-06-13
- Day 1: 2026-06-10
- Day 2: 2026-06-11
- Day 3: 2026-06-12
- Day 4: 2026-06-13

## 정렬

일정 정렬 기준:

1. 날짜
2. 시간
3. order
4. createdAt

사용자가 위/아래 이동을 하면 같은 날짜 안의 `order` 값을 조정한다.



