<script lang="ts">
  import type { Product, TaggedProductArg } from '$lib/storage';
  import Paper, { Content, Title } from '@smui/paper';
  import ProductForm from './product.svelte';
  import { _ } from 'svelte-i18n';
  import Button, { Group } from '@smui/button';
  import IconButton from '@smui/icon-button';
  import { send } from '$lib/utils';

  export let product: TaggedProductArg;
  let output: string | null = null;
</script>

<Paper>
  <Title>
    {$_('Selected product:')}
  </Title>
  <Content>
    <ProductForm bind:value={product} />
    <div class="flex items-center">
      <Button
        variant="raised"
        on:click={async () =>
          (output = await send('Update', [
            { type: 'StrArg', str: String(product.product.id) },
            product
          ]))}>{$_('update')}</Button
      >
      <IconButton
        class="material-icons"
        on:click={async () =>
          (output = await send('RemoveById', [
            { type: 'StrArg', str: String(product.product.id) }
          ]))}>delete</IconButton
      >
    </div>
    {#if output}
      <Paper>
        <Title>{$_('Output:')}</Title>
        <Content>
          {output}
        </Content>
      </Paper>
    {/if}
  </Content>
</Paper>
