<script lang="ts">
  import {
    CountryArr,
    type Arg,
    UnitOfMeasureArr,
    type Person,
    type ProductArg,
    type TaggedProductArg
  } from '$lib/storage';
  import Textfield from '@smui/textfield';
  import Select, { Option } from '@smui/select';
  import Paper from '@smui/paper';
  import { _ } from 'svelte-i18n';

  export let value: TaggedProductArg;

  $: value = value;
  $: console.log('product', value);
</script>

<div class="flex flex-col">
  <Textfield label="Name" bind:value={value.product.name} />
  <Paper variant="outlined">
    <p>{$_('Coordinates')}</p>
    <Textfield label={$_('X')} type="number" bind:value={value.product.coordinates.x} />
    <Textfield label={$_('Y')} type="number" bind:value={value.product.coordinates.y} />
  </Paper>

  <Textfield label={$_('price')} type="number" bind:value={value.product.price} />
  <Textfield
    label={$_('manufactureCost')}
    type="number"
    bind:value={value.product.manufactureCost}
  />

  <Select bind:value={value.product.unitOfMeasure} label={$_('unitOfMeasure')}>
    {#each UnitOfMeasureArr as value}
      <Option {value}>{value}</Option>
    {/each}
  </Select>

  <Paper variant="outlined">
    <p>{$_('Owner')}</p>
    <Textfield label={$_('Name')} bind:value={value.product.owner.name} />
    <Textfield
      label={$_('birthday')}
      type="datetime-local"
      bind:value={value.product.owner.birthday}
    />

    <Select bind:value={value.product.owner.nationality} label={$_('nationality')}>
      {#each CountryArr as value}
        <Option {value}>{value}</Option>
      {/each}
    </Select>
  </Paper>
</div>
