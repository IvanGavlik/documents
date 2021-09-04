@org.hibernate.annotations.GenericGenerator(
  name = "ID_GENERATOR_POOLED",
  strategy = "enhanced-sequence",
  parameters = {
     @org.hibernate.annotations.Parameter(
        name = "sequence_name",
        value = "JPWH_SEQUENCE"
     ),
     @org.hibernate.annotations.Parameter(
        name = "increment_size",
        value = "100"
     ),
     @org.hibernate.annotations.Parameter(
        name = "optimizer",
        value = "pooled-lo"
     )
})

package org.jpwh.model.bulkbatch;