db.getCollection('kpi_category').remove({});
db.getCollection('kpi_category').insert(
[
  {
    "categoryId": "categoryOne",
    "categoryName": "Category One"
  },
  {
    "categoryId": "categoryTwo",
    "categoryName": "Category Two"
  },
  {
    "categoryId": "categoryThree",
    "categoryName": "Category Three"
  }
]);