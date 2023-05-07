<script lang="ts">
  import type { Product, TaggedProductArg } from '$lib/storage';
  import Paper, { Content, Title } from '@smui/paper';
  import ProductForm from './product.svelte';
  import { _ } from 'svelte-i18n';
  import Button, { Group } from '@smui/button';
  import IconButton from '@smui/icon-button';
  import { send } from '$lib/utils';

  export let product: TaggedProductArg;
  export let update: () => Promise<void>;
  export let close: () => void;
  let output: string | null = null;
</script>

<Paper class="relative">
  <div class="absolute right-2 top-3">
    <IconButton on:click={close} class="material-icons">close</IconButton>
  </div>
  <Title>
    {$_('Selected product:')}
  </Title>
  <Content>
    <div class="flex items-center">
      <Button
        variant="raised"
        on:click={async () => {
          output = await send('Update', [
            { type: 'StrArg', str: String(product.product.id) },
            product
          ]);
          update();
        }}>{$_('update')}</Button
      >
      <IconButton
        class="material-icons"
        on:click={async () => {
          output = await send('RemoveById', [{ type: 'StrArg', str: String(product.product.id) }]);
          if (output?.startsWith('removed')) close();
          update();
        }}>delete</IconButton
      >
    </div>
    <ProductForm bind:value={product} />

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
